package deus.momentum.mixin;

import deus.momentum.systems.stamina.IStaminaSettings;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.OptionBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = GameSettings.class, remap = false)
public class GameSettingsMixin implements IStaminaSettings
{
	@Unique
	private final GameSettings mixinInst = (GameSettings)((Object)this);
	@Unique
	public OptionBoolean initialRunSetupFinished = new OptionBoolean(mixinInst, "staminamod.options.initialRunSetupFinished", false);

	@Override
	public OptionBoolean momentum$initialRunSetupFinished()
	{
		return initialRunSetupFinished;
	}
}
