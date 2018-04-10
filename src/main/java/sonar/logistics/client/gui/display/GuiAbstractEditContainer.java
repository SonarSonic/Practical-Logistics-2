package sonar.logistics.client.gui.display;

import java.io.IOException;

import net.minecraft.util.Tuple;
import sonar.core.client.gui.widgets.ScrollerOrientation;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

public class GuiAbstractEditContainer extends GuiAbstractEditScreen {

	public DisplayElementContainer c;
	public SonarScroller scaling_scroller;
	public SonarScroller spacing_scroller;

	public GuiAbstractEditContainer(DisplayElementContainer c, TileAbstractDisplay display) {
		super(c.getGSI(), display);
		this.c = c;
	}

	public void save() {
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createResizeContainerPacket(c.containerIdentity, c.getTranslation(), c.getContainerMaxScaling(), c.percentageScale), -1, c.getGSI());
	}

	public void renderDisplayScreen(float partialTicks, int x, int y) {
		DisplayElementHelper.renderElementStorageHolder(c);
	}

	public double[] getUnscaled() {
		return new double[] { c.getContainerMaxScaling()[0], c.getContainerMaxScaling()[1], 1 };
	}

	public boolean doDisplayScreenClick(double clickX, double clickY, int key) {
		Tuple<IDisplayElement, double[]> click = c.getClickBoxes(new double[] { 0, 0, 0 }, clickX, clickY); // remove adjustment
		if (click != null) {
			DisplayScreenClick fakeClick = GSIHelper.createFakeClick(gsi, c.getTranslation()[0] + clickX, c.getTranslation()[1] + clickY, isDoubleClick(), key);
			onDisplayElementClicked(click.getFirst(), fakeClick, click.getSecond());
			return true;
		}
		return false;
	}

	public void onDisplayElementClicked(IDisplayElement e, DisplayScreenClick fakeClick, double[] subClick) {
		//if (e instanceof IClickableElement) {
		//	((IClickableElement) e).onGSIClicked(fakeClick, mc.player, subClick[0], subClick[1]);
		//}
	}

	@Override
	public void initGui() {
		super.initGui();
		scaling_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151, 16, 80);
		scaling_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);

		spacing_scroller = new SonarScroller(this.guiLeft + 90, this.guiTop + 151 + 20, 16, 80);
		spacing_scroller.setOrientation(ScrollerOrientation.HORIZONTAL);
		setScalingScroller((float) c.percentageScale);
		// setSpacingScroller(text.spacing);
	}

	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		scaling_scroller.drawScreen(x, y, true);
		spacing_scroller.drawScreen(x, y, true);
		setScalingScroller(scaling_scroller.currentScroll);
		setSpacingScroller(spacing_scroller.currentScroll);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.text("TEXT SCALING", 10, 154, -1);
		FontHelper.text("TEXT SPACING", 10, 154 + 20, -1);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
		super.drawGuiContainerBackgroundLayer(partialTicks, x, y);
		renderScroller(scaling_scroller);
		renderScroller(spacing_scroller);
	}

	public void setScalingScroller(float scaling) {
		if (scaling == 0) {
			scaling = 0.01F;
		}
		if (c.percentageScale != scaling || scaling_scroller.currentScroll != scaling) {
			c.percentageScale = scaling;
			scaling_scroller.currentScroll = scaling;
			setContainerScaling();
		}
	}

	public void setSpacingScroller(float scaling) {}
	
	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (isCloseKey(i)) {
			save();
		}
		super.keyTyped(c, i);
	}
}
