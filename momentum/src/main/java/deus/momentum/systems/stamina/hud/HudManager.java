package deus.momentum.systems.stamina.hud;

import net.minecraft.client.gui.hud.component.ComponentAnchor;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.client.gui.hud.component.layout.LayoutSnap;

public class HudManager
{
	public String initialSettingsString;

	public void onInitialize()
	{
		StaminaBarComponent staminaBar = new StaminaBarComponent("stamina", 182, 7, new LayoutSnap(HudComponents.HOTBAR, ComponentAnchor.TOP_LEFT, ComponentAnchor.BOTTOM_LEFT));

		HudComponents.register(staminaBar);

		if (HudComponents.HEALTH_BAR.getLayout() instanceof LayoutSnap layout)
			layout.setParent(staminaBar);

		if (HudComponents.ARMOR_BAR.getLayout() instanceof LayoutSnap layout)
			layout.setParent(staminaBar);

		initialSettingsString = HudComponents.INSTANCE.toSettingsString();
	}
}
