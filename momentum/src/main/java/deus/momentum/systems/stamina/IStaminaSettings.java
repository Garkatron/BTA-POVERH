package deus.momentum.systems.stamina;

import net.minecraft.client.gui.options.components.BooleanOptionComponent;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.OptionBoolean;

public interface IStaminaSettings
{
	OptionBoolean momentum$initialRunSetupFinished();
	KeyBinding momentum$keyGoProne();
}
