package sonar.logistics.core.items.guide.pages.elements;

import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import sonar.logistics.core.items.guide.GuiGuide;
import sonar.logistics.core.items.guide.pages.pages.IGuidePage;

public class ElementImage implements IGuidePageElement {

	public ResourceLocation location;
	public int page;
	public int x, y;
	public int width, height;
	public int minU, minV, maxU, maxV;

	public ElementImage(ResourceLocation location, int page, int x, int y, int minU, int minV, int maxU, int maxV, int width, int height) {
		this.location = location;
		this.page = page;
		this.x = x;
		this.y = y;
		this.minU = minU;
		this.minV = minV;
		this.maxU = maxU;
		this.maxV = maxV;
		this.width = width;
		this.height = height;
	}

	public ElementImage(ResourceLocation location, int page, int x, int y, int minU, int minV, int maxU, int maxV) {
		this(location, page, x, y, minU, minV, maxU, maxV, maxU - minU, maxV - minV);
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
	public void drawElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
	}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		gui.mc.getTextureManager().bindTexture(location);
		// gui.drawTexturedModalRect(x, y, 0, 0 , 64, 64);
		Gui.drawScaledCustomSizeModalRect(x, y, minU, minV, maxU, maxV, width, height, maxU, maxU);

	}

	@Override
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {

	}

	@Override
	public boolean mouseClicked(GuiGuide gui, IGuidePage page, int x, int y, int button) {
		return false;
	}

}
