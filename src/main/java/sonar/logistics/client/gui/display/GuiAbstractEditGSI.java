package sonar.logistics.client.gui.display;

import net.minecraft.util.Tuple;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.displays.tiles.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

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
			DisplayScreenClick fakeClick = GSIHelper.createFakeClick(gsi, clickX, clickY, isDoubleClick(), key);
			onDisplayElementClicked(click.getFirst(), fakeClick, click.getSecond());
			return true;
		}
		return false;
	}

	public void onDisplayElementClicked(IDisplayElement e, DisplayScreenClick fakeClick, double[] subClick) {
		
	}
}
