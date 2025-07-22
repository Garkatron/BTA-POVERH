package deus.momentum.mixin;

import deus.momentum.systems.stamina.IStaminaSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.options.components.KeyBindingComponent;
import net.minecraft.client.gui.options.data.OptionsPage;
import net.minecraft.client.gui.options.data.OptionsPages;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsPages.class)
public class OptionsPagesMixin {
	@Inject(method = "init", at = @At("TAIL"), remap = false)
	private static void addCustomKeyBinding(CallbackInfo ci) {
		OptionsPage controlsPage = OptionsPages.CONTROLS;

		GameSettings settings = Minecraft.getMinecraft().gameSettings;

		controlsPage.getComponents().add(new KeyBindingComponent(((IStaminaSettings)settings).momentum$keyGoProne()));
	}
}
