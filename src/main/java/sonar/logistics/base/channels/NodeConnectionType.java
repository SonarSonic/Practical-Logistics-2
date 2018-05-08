package sonar.logistics.base.channels;

public enum NodeConnectionType {
	TILE(BlockConnection.class), ENTITY(EntityConnection.class);

	public Class<? extends NodeConnection> clazz;

	NodeConnectionType(Class<? extends NodeConnection> clazz) {
		this.clazz = clazz;
	}

	public boolean isTile() {
		return this == TILE;
	}

	public boolean isEntity() {
		return this == ENTITY;
	}

}
