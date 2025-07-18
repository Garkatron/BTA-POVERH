package deus.momentum.mixin;


import deus.momentum.systems.stamina.StaminaConstants;
import deus.momentum.systems.stamina.network.PacketAddStamina;
import deus.momentum.systems.stamina.network.PacketSendStamina;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.world.World;
import net.minecraft.server.entity.player.PlayerServer;
import net.minecraft.server.net.handler.PacketHandlerServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PlayerServer.class, remap = false)
public abstract class PlayerServerMixin extends Player
{
	@Shadow
	public PacketHandlerServer playerNetServerHandler;

	public PlayerServerMixin(World world)
	{
		super(world);
	}


	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type)
	{
		boolean hurtSuccess = super.hurt(attacker, damage, type);

		if (hurtSuccess)
		{
			PacketAddStamina packet = new PacketAddStamina((damage / 2f) * -(StaminaConstants.staminaLossPerHeart));
			this.playerNetServerHandler.sendPacket(packet);
		}

		return hurtSuccess;
	}

	@Override
	public void onDeath(Entity entityKilledBy) {
		super.onDeath(entityKilledBy);
		PacketSendStamina packet = new PacketSendStamina(100, false);
		this.playerNetServerHandler.sendPacket(packet);
	}
}
