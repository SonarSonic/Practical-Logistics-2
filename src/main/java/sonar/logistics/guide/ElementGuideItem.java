package sonar.logistics.guide;

import org.lwjgl.opengl.GL11;

import net.minecraft.item.ItemStack;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;

public class ElementGuideItem implements IGuidePageElement {

	public int page;
	public ItemStack stack;
	public int x, y;

	public ElementGuideItem(int page, ItemStack stack, int x, int y) {
		this.page = page;
		this.stack = stack;
		this.x = x;
		this.y = y;
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y, (int) ((64) * 1 / 0.75), (int) ((64) * 1 / 0.75) };
	}

	@Override
	public void drawElement(GuiGuide gui, IGuidePage page, int x, int y, int pageID) {
		GL11.glPushMatrix();
		GL11.glScaled(4, 4, 4);
		RenderHelper.saveBlendState();
		gui.drawTransparentRect(x, y, x + 16, y + 16, LogisticsColours.blue_overlay.getRGB());
		RenderHelper.restoreBlendState();
		RenderHelper.saveBlendState();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.renderItem(gui, x, y, stack);
		RenderHelper.renderStoredItemStackOverlay(stack, 0, x, y, null, true);
		RenderHelper.restoreBlendState();
		GL11.glScaled(1 / 4, 1 / 4, 1 / 4);
		GL11.glPopMatrix();
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
