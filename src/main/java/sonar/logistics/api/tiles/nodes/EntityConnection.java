package sonar.logistics.api.tiles.nodes;

import java.util.UUID;

import net.minecraft.entity.Entity;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.utils.NodeConnectionType;
import sonar.logistics.info.types.MonitoredEntity;

public class EntityConnection extends NodeConnection<MonitoredEntity> {

	public Entity entity;
	public UUID uuid;

	public EntityConnection(INetworkTile source, Entity entity) {
		super(source);
		this.entity = entity;
		this.uuid = entity.getPersistentID();
	}

	public int hashCode() {
		return entity == null ? -1 : entity.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof EntityConnection) {
			return ((EntityConnection) obj).entity.equals(entity);
		}
		return false;
	}

	public MonitoredEntity getChannel() {
		return new MonitoredEntity(entity);
	}

	@Override
	public NodeConnectionType getType() {
		return NodeConnectionType.ENTITY;
	}
}
