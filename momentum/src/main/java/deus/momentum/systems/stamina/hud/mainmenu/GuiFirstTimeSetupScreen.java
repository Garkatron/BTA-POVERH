package deus.momentum.systems.stamina.hud.mainmenu;

import deus.momentum.Momentum;
import net.minecraft.client.gui.ButtonElement;
import net.minecraft.client.gui.Screen;
import net.minecraft.client.gui.hud.component.HudComponents;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.lang.I18n;
import org.lwjgl.opengl.GL11;

public class GuiFirstTimeSetupScreen extends Screen
{
	private ButtonElement resetHudButton;
	private ButtonElement dontResetHudButton;

	@Override
	public void init()
	{
		resetHudButton = new ButtonElement(0, this.width / 2 - 105, (this.height / 2) + 5, 100, 20, "Reset HUD");
		dontResetHudButton = new ButtonElement(1, this.width / 2 + 5, (this.height / 2) + 5, 100, 20, "Don't Reset HUD");
		buttons.add(resetHudButton);
		buttons.add(dontResetHudButton);
	}

	@Override
	public void render(int mx, int my, float partialTick) {
		GL11.glDisable(2896);
		GL11.glDisable(2912);
		Tessellator tessellator = Tessellator.instance;
		mc.textureManager.loadTexture("/gui/background.png").bind();
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		float f = 32.0f;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(0x404040);
		tessellator.addVertexWithUV(0.0, this.height, 0.0, 0.0, (float)this.height / f);
		tessellator.addVertexWithUV(this.width, this.height, 0.0, (float)this.width / f, (float)this.height / f);
		tessellator.addVertexWithUV(this.width, 0.0, 0.0, (float)this.width / f, 0.0);
		tessellator.addVertexWithUV(0.0, 0.0, 0.0, 0.0, 0.0);
		tessellator.draw();

		I18n loc = I18n.getInstance();

		int lines = 2;

		int titleHeight = (height / 2) - 25;

		// Title
		drawStringCentered(mc.font, loc.translateKey("betterwhenrunning.fts.title"), width / 2, titleHeight - 12, 0xFFFFFF);

		// Lines
		for (int i = 1; i <= lines; i++)
		{
			drawStringCentered(mc.font, loc.translateKey("betterwhenrunning.fts.line" + i), width / 2, (titleHeight + ((12 * (i - 1)))), 0x9E9E9E);
		}

		super.render(mx, my, partialTick);
	}


	@Override
	protected void buttonReleased(ButtonElement button) {
		if (button.id == 0 || button.id == 1)
		{
			mc.displayScreen(null);
		}

		if (button.id == 0)
		{
			HudComponents.INSTANCE.fromSettingsString(Momentum.hudManager.initialSettingsString);
			mc.gameSettings.saveOptions();
		}

		Momentum.options.momentum$initialRunSetupFinished().set(true);
	}
}
