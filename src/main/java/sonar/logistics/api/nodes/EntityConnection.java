package sonar.logistics.api.nodes;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.connections.monitoring.MonitoredEntity;

public class EntityConnection extends NodeConnection<MonitoredEntity> {

	public Entity entity;
	public UUID uuid;

	public EntityConnection(ILogicTile source, Entity entity) {
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
}
