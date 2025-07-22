package deus.momentum.mixin;

import deus.momentum.interfaces.mixin.IPlayerMovementExtra;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.MobRendererPlayer;
import net.minecraft.core.entity.player.Player;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MobRendererPlayer.class, remap = false)
public class PlayerRendererMixin {

	@Inject(method = "setupRotations(Lnet/minecraft/core/entity/player/Player;FFF)V", at = @At("TAIL"), remap = false)
	private void setupProneRotations(Player entity, float ticksExisted, float bodyYaw, float partialTick, CallbackInfo ci) {
		if (((IPlayerMovementExtra) entity).momentum$isProne()) {
//			GL11.glRotatef(180.0F - bodyYaw, 0.0F, 1.0F, 0.0F);
//			GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
		}
	}

	@Inject(method = "translateModel(Lnet/minecraft/core/entity/player/Player;DDD)V", at = @At("TAIL"), remap = false)
	private void adjustProneTranslation(Player entity, double x, double y, double z, CallbackInfo ci) {
		if (((IPlayerMovementExtra) entity).momentum$isProne()) {
			// Ajustar la traslación para alinear el modelo con el suelo (heightOffset = 0.31F)
			//GL11.glTranslatef(0.0F, -1.27F, 0.0F);
		}
	}

}
