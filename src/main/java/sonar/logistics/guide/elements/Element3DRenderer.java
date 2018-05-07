package sonar.logistics.guide.elements;

import org.lwjgl.opengl.GL11;
import sonar.core.client.gui.GuiSonar;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.IGuidePage;
import sonar.logistics.guide.IGuidePageElement;
import sonar.logistics.guide.Logistics3DRenderer;

public class Element3DRenderer implements IGuidePageElement {

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

	public Element3DRenderer(Logistics3DRenderer render, int page, double scale, int x, int y, int width, int height) {
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
		return new int[] { x, y, width, height };
	}

	@Override
	public void drawElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
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
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		GuiSonar.drawTransparentRect(x, y, x + width, y + height, LogisticsColours.blue_overlay.getRGB());
	}

	@Override
	public boolean mouseClicked(GuiGuide gui, IGuidePage page, int x, int y, int button) {
		return false;
	}

}
