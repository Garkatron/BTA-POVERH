package deus.momentum.mixin;

import deus.momentum.systems.stamina.IStaminaSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.option.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin
{
	@Shadow
	public GameSettings gameSettings;

	// @Shadow
	// public abstract void displayGuiScreen(Gui guiscreen);

	@Inject(method = "startGame", at = @At(value = "TAIL"), remap = false)
	private void startOfGameInit(CallbackInfo ci)
	{
		//StaminaMod.options = (IStaminaSettings) gameSettings; 	public static IStaminaSettings options;

//		if (!StaminaMod.options.initialRunSetupFinished().value)
//		{
//			displayGuiScreen(new GuiFirstTimeSetupScreen());
//			System.out.println("First time setup not finished, opening setup screen.");
//		}
	}
}
