package deus.momentum.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import jamdoggie.staminamod.mixininterfaces.IEntityPlayerMixin;
import jamdoggie.staminamod.network.PacketSendStamina;
import net.minecraft.core.net.packet.Packet1Login;
import net.minecraft.server.entity.player.EntityPlayerMP;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.minecraft.server.net.handler.NetLoginHandler.class, remap = false)
public class NetLoginHandlerMixin
{
	@Inject(method = "doLogin", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/server/entity/player/EntityPlayerMP;func_20057_k()V"
	, shift = At.Shift.AFTER, remap = false)
	)
	private void doLogin(Packet1Login packet1login, CallbackInfo ci, @Local EntityPlayerMP entityplayermp)
	{
		IEntityPlayerMixin playerMixin = (IEntityPlayerMixin)entityplayermp;

		System.out.println("Sending stamina packet with stamina: " + playerMixin.getStamina() + " and exhausted: " + playerMixin.isExhausted());

		PacketSendStamina packet = new PacketSendStamina(playerMixin.getStamina(), playerMixin.isExhausted());
		entityplayermp.playerNetServerHandler.sendPacket(packet);
	}
}
