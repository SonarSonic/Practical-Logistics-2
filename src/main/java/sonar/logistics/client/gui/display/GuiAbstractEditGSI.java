package sonar.logistics.client.gui.display;

import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
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

	@Override
	public boolean doDisplayScreenClick(double clickX, double clickY, int key) {				
		return false;
	}

}
