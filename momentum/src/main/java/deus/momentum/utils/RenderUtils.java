package deus.momentum.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.tessellator.Tessellator;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.Gui;

public class RenderUtils
{
	// public static Minecraft mc = Minecraft.getMinecraft();
//	public static void bindTexture(String texture)
//	{
//		int texId = mc.textureManager.bufferedTextures.get(texture);
//		System.out.println("Texture ID: " + texId);
//		mc.textureManager.bindTexture(texId);
//	}

	public static void drawTexturedModalRect(Gui gui, double x, double y, double u, double v, double width, double height, double scale) {
		double uScale = scale;
		double vScale = scale;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x + 0, y + height, gui.zLevel, (u + 0) * uScale, (v + height) * vScale);
		tessellator.addVertexWithUV(x + width, y + height, gui.zLevel, (u + width) * uScale, (v + height) * vScale);
		tessellator.addVertexWithUV(x + width, y + 0, gui.zLevel, (u + width) * uScale, (v + 0) * vScale);
		tessellator.addVertexWithUV(x + 0, y + 0, gui.zLevel, (u + 0) * uScale, (v + 0) * vScale);
		tessellator.draw();
	}
}
