package sonar.logistics.client.gui;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import sonar.core.client.gui.GuiSonar;
import sonar.core.client.gui.GuiSonarTile;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.PL2Constants;
import sonar.logistics.client.LogisticsColours;

public class GuiLogistics extends GuiSonarTile {

	public static final ResourceLocation playerInv = new ResourceLocation(PL2Constants.MODID + ":textures/gui/player_inventory.png");
	public SonarScroller scroller;

	public GuiLogistics(Container container, IWorldPosition entity) {
		super(container, entity);
	}

	@Override
	public ResourceLocation getBackground() {
		Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
		return null;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		RenderHelper.saveBlendState();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTransparentRect(this.guiLeft, this.guiTop, this.guiLeft + this.xSize, this.guiTop + this.ySize, LogisticsColours.layers[1].getRGB());
		drawTransparentRect(this.guiLeft + 1, this.guiTop + 1, this.guiLeft + this.xSize - 1, this.guiTop + this.ySize - 1, LogisticsColours.layers[2].getRGB());
		renderScroller(scroller);
	}

	public void renderScroller(SonarScroller scroller) {
		if (scroller != null && scroller.renderScroller) {
			if (scroller.orientation.isVertical()) {
				int scrollYPos = scroller.top + (int) ((float) (scroller.length - 17) * scroller.getCurrentScroll());
				drawTransparentRect(scroller.left, scroller.top, scroller.left + 8, scroller.top + scroller.length - 2, LogisticsColours.layers[1].getRGB());
				drawTransparentRect(scroller.left, scrollYPos, scroller.left + 8, scrollYPos + 15, LogisticsColours.layers[2].getRGB());
				drawTransparentRect(scroller.left, scrollYPos, scroller.left + 8, scrollYPos + 15, LogisticsColours.layers[2].getRGB());
			}else{
				int scrollXPos = scroller.left + (int) ((float) (scroller.width - 17) * scroller.getCurrentScroll());
				drawTransparentRect(scroller.left, scroller.top, scroller.left + scroller.width, scroller.top + scroller.length - 2, LogisticsColours.layers[1].getRGB());				
				drawTransparentRect(scrollXPos, scroller.top, scrollXPos + 15,scroller.top + 8, LogisticsColours.layers[2].getRGB());
				drawTransparentRect(scrollXPos, scroller.top, scrollXPos + 15,scroller.top + 8,  LogisticsColours.layers[2].getRGB());				
			}
		}
	}

	public void renderPlayerInventory(int xPos, int yPos) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(playerInv);
		drawTexturedModalRect(this.guiLeft + xPos, this.guiTop + yPos, 0, 0, this.xSize, this.ySize);
	}

}