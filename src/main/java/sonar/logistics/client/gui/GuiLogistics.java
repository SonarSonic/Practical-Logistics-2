package sonar.logistics.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import sonar.core.client.gui.GuiSonarTile;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.PL2Constants;
import sonar.logistics.client.LogisticsColours;

import java.util.ArrayList;
import java.util.List;

public class GuiLogistics extends GuiSonarTile {

	private static int currentColour = -1;
	private static List<Integer> lastColours = new ArrayList<>();
	public static final ResourceLocation playerInv = new ResourceLocation(PL2Constants.MODID + ":textures/gui/player_inventory.png");
	public SonarScroller scroller;

	public GuiLogistics(Container container, IWorldPosition entity) {
		super(container, entity);
	}

	public static int getCurrentColour() {
		return currentColour;
	}
	
	public static void setCurrentColour(int colour){
		currentColour = colour;
	}
	
	public static void setCurrentColourAndSaveLast(int colour){
		lastColours.add(0, currentColour);
		currentColour = colour;
	}

	public static List<Integer> getLastColours() {
		return lastColours;
	}

	@Override
	public ResourceLocation getBackground() {
		Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
		return null;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		drawTransparentRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, LogisticsColours.layers[1].getRGB());
		drawTransparentRect(this.guiLeft + 1, this.guiTop + 1, this.guiLeft + this.xSize - 1, this.guiTop + this.ySize - 1, LogisticsColours.layers[2].getRGB());
		renderScroller(scroller);
	}

	public void renderScroller(SonarScroller scroller) {
		if (scroller != null && scroller.renderScroller) {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			if (scroller.orientation.isVertical()) {
				int scrollYPos = scroller.top + (int) ((float) (scroller.length - 17) * scroller.getCurrentScroll());
				drawTransparentRect(scroller.left, scroller.top, scroller.left + 8, scroller.top + scroller.length - 2, LogisticsColours.layers[1].getRGB());
				drawTransparentRect(scroller.left, scrollYPos, scroller.left + 8, scrollYPos + 15, LogisticsColours.layers[2].getRGB());
				drawTransparentRect(scroller.left, scrollYPos, scroller.left + 8, scrollYPos + 15, LogisticsColours.layers[2].getRGB());
				drawTransparentRect(scroller.left, scrollYPos, scroller.left + 8, scrollYPos + 15, LogisticsColours.layers[2].getRGB());
			} else {
				int scrollXPos = scroller.left + (int) ((float) (scroller.width - 15) * scroller.getCurrentScroll());
				drawTransparentRect(scroller.left, scroller.top, scroller.left + scroller.width, scroller.top + scroller.length - 2, LogisticsColours.layers[1].getRGB());

				drawTransparentRect(scrollXPos, scroller.top, scrollXPos + 15, scroller.top + scroller.length - 2, LogisticsColours.layers[2].getRGB());
				drawTransparentRect(scrollXPos, scroller.top, scrollXPos + 15, scroller.top + scroller.length - 2, LogisticsColours.layers[2].getRGB());
				drawTransparentRect(scrollXPos, scroller.top, scrollXPos + 15, scroller.top + scroller.length - 2, LogisticsColours.layers[2].getRGB());
			}
		}
	}

	public void renderPlayerInventory(int xPos, int yPos) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(playerInv);
		drawTexturedModalRect(this.guiLeft + xPos, this.guiTop + yPos, 0, 0, this.xSize, this.ySize);
	}

}