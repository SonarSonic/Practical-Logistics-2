package sonar.logistics.api.displays;

import java.util.List;

import sonar.core.helpers.FontHelper;
import sonar.core.utils.CustomColour;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.client.gsi.GSIElementPacketHelper;

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
			gsi.startResizeSelectionMode(containers.get(0));			
			break;
		default:
			break;

		}
	}
}
