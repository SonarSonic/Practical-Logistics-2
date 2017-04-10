package sonar.logistics.api.utils;

import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;

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
