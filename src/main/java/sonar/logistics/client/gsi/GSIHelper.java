package sonar.logistics.client.gsi;

import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.helpers.DisplayElementHelper;

public class GSIHelper {

	//// GRID SELECTION MODE \\\\\

	public static double getGridXScale(DisplayGSI gsi) {
		return Math.max(gsi.getDisplayScaling()[0] / 8, gsi.display.getDisplayType().width / 4);
	}

	public static double getGridYScale(DisplayGSI gsi) {
		return Math.max(gsi.getDisplayScaling()[1] / 8, gsi.display.getDisplayType().height / 4);
	}

	public static double getGridXPosition(DisplayGSI gsi, double x) {
		return DisplayElementHelper.toNearestMultiple(x, gsi.getDisplayScaling()[0], getGridXScale(gsi));
	}

	public static double getGridYPosition(DisplayGSI gsi, double y) {
		return DisplayElementHelper.toNearestMultiple(y, gsi.getDisplayScaling()[1], getGridYScale(gsi));
	}
	
	public static final DisplayScreenClick createFakeClick(DisplayElementContainer c, double clickX, double clickY, boolean doubleClick, int key) {
		DisplayScreenClick fakeClick = new DisplayScreenClick();
		fakeClick.gsi = c.getGSI();
		fakeClick.type = key == 0 ? BlockInteractionType.LEFT : BlockInteractionType.RIGHT;
		fakeClick.clickX = clickX;
		fakeClick.clickY = clickY;
		fakeClick.clickPos = c.getGSI().getDisplay().getActualDisplay().getCoords().getBlockPos();
		fakeClick.identity = c.getGSI().getDisplayGSIIdentity();
		fakeClick.doubleClick = false;
		fakeClick.fakeGuiClick = true;
		return fakeClick;
	}
}
