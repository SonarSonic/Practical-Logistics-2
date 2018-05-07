package sonar.logistics.api.displays.elements;

import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.client.gsi.GSIElementPacketHelper;

import java.util.List;

public enum ElementSelectionType {
	DELETE(), RESIZE, EDIT;

	/** if you can select more than one / if the action needs to be confirmed */
	public boolean shouldCollect() {
		return this == DELETE;
	}

	public int getTypeColour() {
		switch (this) {
		case DELETE:
			return FontHelper.getIntFromColor(100, 50, 50);
		case EDIT:
			return FontHelper.getIntFromColor(50, 100, 50);
		case RESIZE:
			return FontHelper.getIntFromColor(50, 50, 100);
		}
		return -1;

	}

	public void finishSelection(DisplayGSI gsi, List<Integer> containers) {
		switch (this) {
		case DELETE:
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createDeleteContainersPacket(containers), -1, gsi);
			break;
		case EDIT:
			// send edit packet
			break;
		case RESIZE:
			gsi.grid_mode.startResizeSelectionMode(containers.get(0));			
			break;
		default:
			break;

		}
	}
}
