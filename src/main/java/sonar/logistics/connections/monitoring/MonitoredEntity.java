package sonar.logistics.connections.monitoring;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.entity.Entity;
import sonar.core.api.utils.BlockCoords;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.logistics.ComparableObject;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.BaseInfo;

@LogicInfoType(id = MonitoredEntity.id, modid = Logistics.MODID)
public class MonitoredEntity extends BaseInfo<MonitoredEntity> implements INameableInfo<MonitoredEntity>, IComparableInfo<MonitoredEntity> {

	public static final String id = "entity";
	public static LogicMonitorHandler<MonitoredEntity> handler = LogicMonitorHandler.instance(ChannelMonitorHandler.id);
	public SyncUUID uuid = new SyncUUID(1);
	public SyncTagType.STRING unlocalizedName = new SyncTagType.STRING(2);
	public SyncTagType.INT dimension = new SyncTagType.INT(3);
	{
		syncList.addParts(uuid, unlocalizedName, dimension);
	}

	public MonitoredEntity() {}
	
	public MonitoredEntity(UUID id, String name, int dim) {
		this.uuid.setObject(id);
		this.unlocalizedName.setObject(name);
		this.dimension.setObject(dim);
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
	public boolean isMatchingType(IMonitorInfo info) {
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
	public LogicMonitorHandler<MonitoredEntity> getHandler() {
		return handler;
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

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, getClientIdentifier(), getClientObject());
	}

	@Override
	public ArrayList<ComparableObject> getComparableObjects(ArrayList<ComparableObject> objects) {
		objects.add(new ComparableObject(this, "uuid", uuid.getUUID()));
		objects.add(new ComparableObject(this, "dimension", dimension.getObject()));
		objects.add(new ComparableObject(this, "name", unlocalizedName.getObject()));
		return objects;
	}
}