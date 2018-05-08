package sonar.logistics.core.tiles.displays.info.types.channels;

import net.minecraft.entity.Entity;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.IComparableInfo;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.INameableInfo;
import sonar.logistics.api.core.tiles.displays.info.comparators.ComparableObject;
import sonar.logistics.base.channels.EntityConnection;
import sonar.logistics.core.tiles.displays.info.types.BaseInfo;

import java.util.List;
import java.util.UUID;

@ASMInfo(id = MonitoredEntity.id, modid = PL2Constants.MODID)
public class MonitoredEntity extends BaseInfo<MonitoredEntity> implements INameableInfo<MonitoredEntity>, IComparableInfo<MonitoredEntity> {

	public static final String id = "entity";
	private SyncUUID uuid = new SyncUUID(1);
	private SyncTagType.STRING unlocalizedName = new SyncTagType.STRING(2);
	private SyncTagType.INT dimension = new SyncTagType.INT(3);
	{
		syncList.addParts(uuid, unlocalizedName, dimension);
	}

	public MonitoredEntity() {}
	
	public MonitoredEntity(UUID id, String name, int dim) {
		this.uuid.setObject(id);
		this.unlocalizedName.setObject(name);
		this.dimension.setObject(dim);
	}

	public MonitoredEntity(EntityConnection connection) {
		this.uuid.setObject(connection.uuid);
		this.unlocalizedName.setObject(connection.name);
		this.dimension.setObject(connection.dim);
	}

	public MonitoredEntity(Entity entity) {
		this.uuid.setObject(entity.getPersistentID());
		this.unlocalizedName.setObject(entity.getName());
		this.dimension.setObject(entity.dimension);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredEntity info) {
		return true;
	}

	@Override
	public boolean isMatchingInfo(MonitoredEntity info) {
		return info.uuid.getUUID().equals(uuid.getUUID());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof MonitoredEntity;
	}

	@Override
	public String getClientIdentifier() {
		return unlocalizedName.getObject();
	}


	@Override
	public String getClientObject() {		
		return "D: " + dimension.getObject();
	}

	@Override
	public String getClientType() {
		return "entity";
	}

	public boolean equals(Object obj) {
		if (obj instanceof MonitoredEntity) {
			MonitoredEntity monitoredCoords = (MonitoredEntity) obj;
			return isMatchingInfo(monitoredCoords);
		}
		return false;
	}

	@Override
	public boolean isValid() {
		return uuid.getUUID() != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public MonitoredEntity copy() {
		return new MonitoredEntity(uuid.getUUID(), unlocalizedName.getObject(), dimension.getObject());
	}

	public UUID getUUID() {
		return uuid.getUUID();
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		objects.add(new ComparableObject(this, "uuid", uuid.getUUID()));
		objects.add(new ComparableObject(this, "dimension", dimension.getObject()));
		objects.add(new ComparableObject(this, "name", unlocalizedName.getObject()));
		return objects;
	}
}