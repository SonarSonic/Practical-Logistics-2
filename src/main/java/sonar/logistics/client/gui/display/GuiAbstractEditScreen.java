package sonar.logistics.client.gui.display;

import static net.minecraft.client.renderer.GlStateManager.scale;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Tuple;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiLogistics;
import sonar.logistics.client.gui.textedit.GuiStyledStringFunctions;
import sonar.logistics.client.gui.textedit.hotkeys.GuiActions;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InteractionHelper;

public abstract class GuiAbstractEditScreen extends GuiLogistics {

	public DisplayGSI gsi;
	public double userScaling = 1, percentageFill = 1, actualLeft, actualTop, actualElementScale, actualElementWidth, actualElementHeight;
	public double[] scaling = new double[] { 0, 0, 0 };
	public long lastClickTime;
	public TileAbstractDisplay display;

	public GuiAbstractEditScreen(DisplayGSI gsi, TileAbstractDisplay display) {
		super(new ContainerMultipartSync(display), gsi.getDisplay());
		this.display = display;
		this.gsi = gsi;
	}

	@Override
	public void initGui() {
		super.initGui();
		xSize = 256;
		ySize = 256;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		setContainerScaling();
	}

	/** get unscaled */
	public abstract double[] getUnscaled();

	/** all translation scaling applied */
	public abstract void renderDisplayScreen(float partialTicks, int x, int y);

	public abstract boolean doDisplayScreenClick(double clickX, double clickY, int key);

	public void setContainerScaling() {
		scaling = new double[] { (xSize) - 8, ((ySize / 2)) - 8, 1 };
		double[] unscaled = getUnscaled();
		actualLeft = (guiLeft) + 4;
		actualTop = (guiTop) + 4;
		actualElementScale = Math.min(scaling[0] / unscaled[0], scaling[1] / unscaled[1]);
		actualElementWidth = (unscaled[0] * actualElementScale) * percentageFill;
		actualElementHeight = (unscaled[1] * actualElementScale) * percentageFill;
		gsi.updateScaling();
		if (this instanceof GuiStyledStringFunctions)
			GuiActions.UPDATE_TEXT_SCALING.trigger((GuiStyledStringFunctions) this);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		GlStateManager.color(1f, 1f, 1f, 1f);

		/// render the edit tools background
		drawTransparentRect(this.guiLeft, this.guiTop + xSize / 2 + 20, this.guiLeft + this.xSize, this.guiTop + xSize / 2 + 20 + 80, LogisticsColours.layers[1].getRGB());
		drawTransparentRect(this.guiLeft + 1, this.guiTop + 1 + xSize / 2 + 20, this.guiLeft + this.xSize - 1, this.guiTop + xSize / 2 + 20 + 80 - 1, LogisticsColours.layers[2].getRGB());

		double pixel = 0.625;
		double[] actualScaling = getActualScaling();
		GlStateManager.pushMatrix();
		DisplayElementHelper.align(getAlignmentTranslation());
		//// SCREEN BORDER & BACKGROUND \\\\
		DisplayElementHelper.drawRect(-pixel, -pixel, actualScaling[0] + pixel, actualScaling[1] + pixel, new CustomColour(174, 227, 227).getRGB());
		DisplayElementHelper.drawRect(0, 0, actualScaling[0], actualScaling[1], new CustomColour(40, 40, 40).getRGB());
		GlStateManager.scale(actualScaling[2], actualScaling[2], actualScaling[2]);
		GlStateManager.translate(0, 0, 1);
		scale(1, 1, -1);
		renderDisplayScreen(partialTicks, x, y);
		GlStateManager.popMatrix();
	}

	public final double[] getAlignmentTranslation() {
		double x = (actualLeft + (scaling[0] - actualElementWidth) / 2) + ((1 - userScaling) * actualElementWidth / 2);
		double y = (actualTop + (scaling[1] - actualElementHeight) / 2) + ((1 - userScaling) * actualElementHeight / 2);
		double z = 0.001;
		return new double[] { x, y, z };
	}

	public final double[] getActualScaling() {
		return new double[] { actualElementWidth * userScaling, actualElementHeight * userScaling, actualElementScale * userScaling };
	}

	public boolean skipContainerClick() {
		return false;
	}

	@Override
	public void mouseClicked(int x, int y, int key) throws IOException {
		Tuple<Boolean, double[]> canClick = canClickContainer(x, y);
		if (skipContainerClick() || !canClick.getFirst() || !doDisplayScreenClick(canClick.getSecond()[0], canClick.getSecond()[1], key)) {
			super.mouseClicked(x, y, key);
		}
	}

	public final Tuple<Boolean, double[]> canClickContainer(int x, int y) {
		double startX = getAlignmentTranslation()[0];
		double startY = getAlignmentTranslation()[1];
		double endX = startX + getActualScaling()[0];
		double endY = startY + getActualScaling()[1];
		if (InteractionHelper.checkClick(x, y, new double[] { startX, startY, endX, endY })) {
			double clickX = (x - startX) / getActualScaling()[2];
			double clickY = (y - startY) / getActualScaling()[2];
			return new Tuple(true, new double[] { clickX, clickY });
		}
		return new Tuple(false, new double[2]);
	}

	public final boolean isDoubleClick() {
		boolean isDoubleClick = false;
		if (Minecraft.getMinecraft().world.getTotalWorldTime() - lastClickTime < 5) {
			isDoubleClick = true;
		}
		lastClickTime = Minecraft.getMinecraft().world.getTotalWorldTime();
		return isDoubleClick;
	}
}
