package sonar.logistics.guide;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.client.gui.LogisticsButton;

public class Guide3DRenderer implements IGuidePageElement {

	public Logistics3DRenderer render;
	public int page;
	public static double rotate = 180;
	public static double rotateY = 0;
	public double scale;
	public int x, y;
	public int width, height;
	public static boolean canRotate = true;

	public static void reset() {
		rotate = 180;
		rotateY = 0;
		canRotate = true;
	}

	public Guide3DRenderer(Logistics3DRenderer render, int page, double scale, int x, int y, int width, int height) {
		this.render = render;
		this.page = page;
		this.scale = scale;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public int getDisplayPage() {
		return page;
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y - 1, (int) (width * 1 / 0.75), (int) (height * 1 / 0.75) };
	}

	@Override
	public void drawElement(GuiGuide gui, IGuidePage page, int x, int y, int pageID) {
		GL11.glPushMatrix();
		RenderHelper.saveBlendState();
		gui.drawTransparentRect(x, y, x + width, y + height, LogisticsColours.blue_overlay.getRGB());
		RenderHelper.restoreBlendState();
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(x + width / 2, y + height / 2, 640);
		GL11.glRotated(-25 + rotateY, 1, 0, 0);
		GL11.glRotated(rotate, 0, 1, 0);
		GL11.glScaled(scale, -scale, scale);
		render.renderInGui();
		if (canRotate) {
			rotate += 0.25;
			if (rotate == 360) {
				rotate = 0;
			}
		}
		GL11.glPopMatrix();
	}

	@Override
	public boolean mouseClicked(GuiGuide gui, IGuidePage page, int x, int y, int button) {
		return false;
	}

}
