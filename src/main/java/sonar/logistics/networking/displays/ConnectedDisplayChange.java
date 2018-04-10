package sonar.logistics.networking.displays;

import java.util.List;

import sonar.logistics.api.tiles.displays.ConnectedDisplay;

public enum ConnectedDisplayChange {
	SUB_DISPLAY_CHANGED(DisplayHandler::updateLargeDisplays), // LARGE DISPLAY ADDED/DISCONNECTED
	SUB_NETWORK_CHANGED(DisplayHandler::updateConnectedNetworks), // LARGE DISPLAYS CONNECTS/DISCONNECTS FROM A NETWORK
	WATCHERS_CHANGED(DisplayHandler::updateWatchers); // WHEN PEOPLE WATCHING THIS DISPLAY HAVE CHANGED

	/** returns if the rest of the updates should continue */
	public ChangeLogic changeLogic;

	ConnectedDisplayChange(ChangeLogic changeLogic) {
		this.changeLogic = changeLogic;
	}

	public boolean shouldRunChange(List<ConnectedDisplayChange> changes) {
		if (changes.contains(this)) {
			return true;
		}
		switch (this) {
		case SUB_DISPLAY_CHANGED:
			break;
		case SUB_NETWORK_CHANGED:
			break;
		case WATCHERS_CHANGED:
			break;
		default:
			break;
		}
		return false;
	}

	/** you may add to the list of current changes, but it will only run ones preceding this in the order of the enum, using ConnectedDisplayHandler.markConnectedDisplayChanged will work the same */
	public boolean doChange(List<ConnectedDisplayChange> changes, ConnectedDisplay change) {
		return changeLogic.doChange(changes, change);
	}

	public static interface ChangeLogic {

		public boolean doChange(List<ConnectedDisplayChange> changes, ConnectedDisplay change);
	}

}