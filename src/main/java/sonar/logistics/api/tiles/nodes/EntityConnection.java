package sonar.logistics.api.tiles.nodes;

import java.util.UUID;

import net.minecraft.entity.Entity;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.info.types.MonitoredEntity;

public class EntityConnection extends NodeConnection<MonitoredEntity> {

	public Entity entity;
	public UUID uuid;
	public String name;
	public int dim;

	public EntityConnection(INetworkTile source, Entity entity) {
		super(source);
		this.uuid = entity.getPersistentID();
		this.name = entity.getName();
		this.dim = entity.dimension;
		this.entity = entity;
	}

	public int hashCode() {
		return uuid.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof EntityConnection) {
			return ((EntityConnection) obj).uuid.equals(uuid);
		}
		return false;
	}

	public MonitoredEntity getChannel() {
		return new MonitoredEntity(this);
	}

	@Override
	public NodeConnectionType getType() {
		return NodeConnectionType.ENTITY;
	}	
	
}
