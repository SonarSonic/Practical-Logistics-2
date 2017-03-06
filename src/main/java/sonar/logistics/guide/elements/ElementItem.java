package sonar.logistics.guide.elements;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.IGuidePage;
import sonar.logistics.guide.IGuidePageElement;

public class ElementItem implements IGuidePageElement {

	public int page;
	public ItemStack stack;
	public int x, y;

	public ElementItem(int page, ItemStack stack, int x, int y) {
		this.page = page;
		this.stack = stack;
		this.x = x;
		this.y = y;
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y, (int) ((54) * 1 / 0.75), (int) ((54) * 1 / 0.75)  };
	}

	@Override
	public void drawElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		GL11.glPushMatrix();
		GlStateManager.pushAttrib();
		double scale = (double) 54 / 16;
		GL11.glTranslated(x, y, 0);
		GL11.glScaled(scale, scale, scale);
		RenderHelper.saveBlendState();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.renderItem(gui, 0, 0, stack);
		RenderHelper.renderStoredItemStackOverlay(stack, 0, 0, 0, null, true);
		RenderHelper.restoreBlendState();
		GL11.glScaled(1 / scale, 1 / scale, 1 / scale);
		GlStateManager.popAttrib();
		GL11.glPopMatrix();
		
	}

	@Override
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		RenderHelper.saveBlendState();
		gui.drawTransparentRect(x, y, x + 54, y + 54, LogisticsColours.blue_overlay.getRGB());
		RenderHelper.restoreBlendState();
		
	}

	@Override
	public boolean mouseClicked(GuiGuide gui, IGuidePage page, int x, int y, int button) {
		return false;
	}

	@Override
	public int getDisplayPage() {
		return page;
	}

}
