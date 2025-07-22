package deus.momentum.mixin;

import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import deus.momentum.interfaces.mixin.IPlayerStamina;
import deus.momentum.systems.stamina.IStaminaSettings;
import deus.momentum.systems.stamina.network.PacketSendStamina;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.input.PlayerInput;
import net.minecraft.core.data.gamerule.GameRules;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PlayerLocal.class, remap = false)
public abstract class PlayerLocalMixin extends Player implements IPlayerMovementExtra
{
	@Shadow
	protected Minecraft mc;

	@Shadow
	public PlayerInput input;

	@Shadow
	protected abstract boolean isBlockTranslucent(int x, int y, int z);

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

		if (((IStaminaSettings)mc.gameSettings).momentum$keyGoProne().isPressed()) {
			momentum$goProne();
		} else {
			setSharedFlag(5, false);
		}

	}
	@Inject(method = "checkInTile", at = @At("HEAD"), cancellable = true, remap = false)
	private void modifyCheckInTile(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (this.noPhysics) {
			cir.setReturnValue(false);
			return;
		}

		int blockX = MathHelper.floor(x);
		int blockY = MathHelper.floor(y);
		int blockZ = MathHelper.floor(z);
		double d3 = x - (double)blockX;
		double d4 = z - (double)blockZ;

		// Check for prone or dwarf mode to skip blockY + 1 check
		boolean isLowHeight = this.isDwarf || ((IPlayerMovementExtra)this).momentum$isProne();
		if (this.isBlockTranslucent(blockX, blockY, blockZ) || (!isLowHeight && this.isBlockTranslucent(blockX, blockY + 1, blockZ))) {
			boolean flag = !this.isBlockTranslucent(blockX - 1, blockY, blockZ) && (!isLowHeight && !this.isBlockTranslucent(blockX - 1, blockY + 1, blockZ));
			boolean flag1 = !this.isBlockTranslucent(blockX + 1, blockY, blockZ) && (!isLowHeight && !this.isBlockTranslucent(blockX + 1, blockY + 1, blockZ));
			boolean flag2 = !this.isBlockTranslucent(blockX, blockY, blockZ - 1) && (!isLowHeight && !this.isBlockTranslucent(blockX, blockY + 1, blockZ - 1));
			boolean flag3 = !this.isBlockTranslucent(blockX, blockY, blockZ + 1) && (!isLowHeight && !this.isBlockTranslucent(blockX, blockY + 1, blockZ + 1));

			byte byte0 = -1;
			double d5 = 9999.0;
			if (flag && d3 < d5) {
				d5 = d3;
				byte0 = 0;
			}
			if (flag1 && 1.0 - d3 < d5) {
				d5 = 1.0 - d3;
				byte0 = 1;
			}
			if (flag2 && d4 < d5) {
				d5 = d4;
				byte0 = 4;
			}
			if (flag3 && 1.0 - d4 < d5) {
				d5 = 1.0 - d4;
				byte0 = 5;
			}

			float f = 0.1F;
			if (byte0 == 0) {
				this.xd = (double)(-f);
			}
			if (byte0 == 1) {
				this.xd = (double)f;
			}
			if (byte0 == 4) {
				this.zd = (double)(-f);
			}
			if (byte0 == 5) {
				this.zd = (double)f;
			}
		}

		cir.setReturnValue(false);
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

	@Override
	public void momentum$goProne() {
		setSharedFlag(5, true);
	}

	@Override
	public boolean momentum$isProne() {
		return getSharedFlag(5);
	}
}
