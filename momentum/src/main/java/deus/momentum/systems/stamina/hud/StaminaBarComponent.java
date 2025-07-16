package deus.momentum.systems.stamina.hud;

import jamdoggie.staminamod.mixininterfaces.IEntityPlayerMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.hud.ComponentAnchor;
import net.minecraft.client.gui.hud.Layout;
import net.minecraft.client.gui.hud.MovableHudComponent;
import net.minecraft.client.gui.hud.component.HudComponentMovable;
import net.minecraft.client.gui.hud.component.layout.LayoutSnap;
import net.minecraft.core.player.gamemode.Gamemode;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class StaminaBarComponent extends HudComponentMovable
{
	private int width = 182;
	private int height = 5;
	private final String texture = "assets/staminamod/textures/gui/stamina_bar.png";

	public Color defaultColor = new Color(24, 240, 81,255);
	public Color exhaustedColor = new Color(240,64,24,255);
	public Color flashColor = new Color(161, 35, 6, 255);

	private float flashTime = 0.0f;
	private long previousTime = 0;

	public StaminaBarComponent(String key, int xSize, int ySize, LayoutSnap layout)
	{
		super(key, xSize, ySize, layout);
		previousTime = System.currentTimeMillis();
	}

	@Override
	public boolean isVisible(Minecraft mc)
	{
		IEntityPlayerMixin player = (IEntityPlayerMixin) mc.thePlayer;

		if (player == null)
			return true;

		return (player.getStamina() < 100f || mc.thePlayer.isSprinting()) && mc.gameSettings.immersiveMode.drawHotbar() && mc.thePlayer.getGamemode()
			.equals(Gamemode.survival);
	}

	private void renderStaminaBar(Minecraft mc, Gui gui, int x, int y, float staminaFloat, boolean exhausted, float partialTick)
	{
		float delta = (System.currentTimeMillis() - previousTime) / 1000f;
		flashTime += delta * 16f;

		GL11.glDisable(GL11.GL_BLEND);

		if (!exhausted)
			setColor(defaultColor);
		else
			setColor(lerpColor(exhaustedColor, flashColor,
				Math.min(
					Math.max(
						((float)Math.sin(flashTime) + 1) / 2.0f,
					0.0f),
				1.0f)));

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture(texture));

		gui.drawTexturedModalRect(x, y, 0, 0, width, height);
		gui.drawTexturedModalRect(x, y, 0, height, (int) (staminaFloat * width), height);
	}

	@Override
	public int getAnchorY(ComponentAnchor anchor)
	{
		return this.isVisible(Minecraft.getMinecraft(this)) ? super.getAnchorY(anchor) : 0;
	}

	@Override
	public void render(Minecraft mc, GuiIngame gui, int xSizeScreen, int ySizeScreen, float partialTick)
	{
		int x = this.getLayout().getComponentX(mc, this, xSizeScreen);
		int y = this.getLayout().getComponentY(mc, this, ySizeScreen);

		IEntityPlayerMixin player = (IEntityPlayerMixin) mc.thePlayer;

		renderStaminaBar(mc, gui, x, y, lerp(player.getPrevStamina() / 100f, player.getStamina() / 100f, partialTick), player.isExhausted(), partialTick);
		previousTime = System.currentTimeMillis();
	}

	@Override
	public void renderPreview(Minecraft mc, Gui gui, Layout layout, int xSizeScreen, int ySizeScreen)
	{
		int x = layout.getComponentX(mc, this, xSizeScreen);
		int y = layout.getComponentY(mc, this, ySizeScreen);

		renderStaminaBar(mc, gui, x, y, 1.0f, false, 0.0f);
	}

	private void setColor(Color color)
	{
		GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
	}

	public Color lerpColor(Color a, Color b, float t)
	{
		return new Color(
			(int) (a.getRed() + (b.getRed() - a.getRed()) * t),
			(int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
			(int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t),
			(int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
	}

	public float lerp(float a, float b, float t)
	{
		return a + (b - a) * t;
	}
}
