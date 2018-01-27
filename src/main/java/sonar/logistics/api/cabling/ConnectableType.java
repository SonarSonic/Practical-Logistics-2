package sonar.logistics.api.cabling;

/** used to distinguish the different types of Cable Connections, used on server side. */
public enum ConnectableType {
	/** for standard Data Cables which are limited to one channel */
	CONNECTABLE, //
	/** for {@link ILogicTile} which can connect to a network */
	TILE, //
	/** for when there is no type of connection whatsoever, null should not be used! */
	SCREEN, //
	REDSTONE, //
	NONE;//

	/** @param type given CableType
	 * @return if the given CableType can connect to the current one. */
	public boolean canConnect(ConnectableType type) {
		if (type == NONE || this == null) {
			return false;
		}
		switch (this) {
		case TILE:
			return true;
		default:
			if (type == TILE) {
				return true;
			}
			return type == this;
		}
	}

	public boolean isRedstone() {
		return this == REDSTONE;
	}

	public boolean isData() {
		return this == CONNECTABLE || this == TILE;
	}

	public boolean isScreen() {
		return this == SCREEN;
	}
}
