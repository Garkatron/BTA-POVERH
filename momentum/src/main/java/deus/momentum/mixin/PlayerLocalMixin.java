package deus.momentum.mixin;

import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import deus.momentum.interfaces.mixin.IPlayerStamina;
import deus.momentum.systems.stamina.network.PacketSendStamina;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.input.PlayerInput;
import net.minecraft.core.data.gamerule.GameRules;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerLocal.class, remap = false)
public abstract class PlayerLocalMixin extends Player implements IPlayerMovementExtra
{
	@Shadow
	protected Minecraft mc;

	@Shadow
	public PlayerInput input;

	public PlayerLocalMixin(World world)
	{
		super(world);
	}

	@Inject(method = "onLivingUpdate()V", at = @At("TAIL"), remap = false)
	private void playerSPTick(CallbackInfo ci)
	{
		IPlayerStamina playerMixin = (IPlayerStamina)(Object)this;

		if (playerMixin.momentum$getStamina() != playerMixin.momentum$getPrevStamina())
		{
			if (this.mc.getSendQueue() != null)
				this.mc.getSendQueue().addToSendQueue(new PacketSendStamina(playerMixin.momentum$getStamina(), playerMixin.momentum$isExhausted()));
		}

		float f = 0.8F;
		boolean forwardInput = this.input.moveForward >= f;
		boolean canSprint = this.world.getGameRuleValue(GameRules.ALLOW_SPRINTING) && !this.noPhysics;

		if (!onGround && forwardInput && !this.momentum$isSprintingOnAir() && canSprint && this.mc.gameSettings.keySprint.isPressed()) {
			this.momentum$setSprintingOnAir();
		} else if (onGround || !forwardInput || !canSprint) {
			this.setSharedFlag(4, false);
		}
	}


	@Override
	public void sendMessage(String message) {
		this.mc.hudIngame.addChatMessage(message);
	}

	@Override
	public void sendStatusMessage(String message) {
		this.mc.hudIngame.heldItemTooltipElement.setString(message);
	}

	@Override
	public void momentum$setSprintingOnAir() {
		setSharedFlag(4,true);
	}

	@Override
	public boolean momentum$isSprintingOnAir() {
		return getSharedFlag(4);
	}
}
