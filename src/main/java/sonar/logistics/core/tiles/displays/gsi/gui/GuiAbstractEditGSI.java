package sonar.logistics.core.tiles.displays.gsi.gui;

import net.minecraft.util.Tuple;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

public class GuiAbstractEditGSI extends GuiAbstractEditScreen {

	public GuiAbstractEditGSI(DisplayGSI gsi, TileAbstractDisplay display) {
		super(gsi, display);
	}

	@Override
	public double[] getUnscaled() {
		return new double[] { gsi.getDisplayScaling()[0], gsi.getDisplayScaling()[1], 1 };
	}

	@Override
	public void renderDisplayScreen(float partialTicks, int x, int y) {
		gsi.getViewableContainers().filter(c -> !gsi.isEditContainer(c)).forEach(DisplayElementContainer::render);
	}

	public boolean doDisplayScreenClick(double clickX, double clickY, int key) {
		Tuple<IDisplayElement, double[]> click = gsi.getElementFromXY(clickX, clickY); // remove adjustment
		if (click != null) {
			DisplayScreenClick fakeClick = DisplayVectorHelper.createFakeClick(gsi, clickX, clickY, isDoubleClick(), key);
			onDisplayElementClicked(click.getFirst(), fakeClick, click.getSecond());
			return true;
		}
		return false;
	}

	public void onDisplayElementClicked(IDisplayElement e, DisplayScreenClick fakeClick, double[] subClick) {}
}
