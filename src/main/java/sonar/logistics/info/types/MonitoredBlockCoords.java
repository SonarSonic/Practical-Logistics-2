package sonar.logistics.info.types;

import java.util.List;

import sonar.core.api.utils.BlockCoords;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.connections.handlers.ChannelNetworkHandler;
import sonar.logistics.connections.handlers.DefaultNetworkHandler;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = MonitoredBlockCoords.id, modid = PL2Constants.MODID)
public class MonitoredBlockCoords extends BaseInfo<MonitoredBlockCoords> implements INameableInfo<MonitoredBlockCoords>, IComparableInfo<MonitoredBlockCoords> {

	public static final String id = "coords";
	public SyncCoords syncCoords = new SyncCoords(1);
	public SyncTagType.STRING unlocalizedName = new SyncTagType.STRING(2);
	{
		syncList.addParts(syncCoords, unlocalizedName);
	}

	public MonitoredBlockCoords() {}

	public MonitoredBlockCoords(BlockCoords coords, String unlocalizedName) {
		this.syncCoords.setCoords(coords);
		this.unlocalizedName.setObject(unlocalizedName);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredBlockCoords info) {
		return true;
	}

	@Override
	public boolean isMatchingInfo(MonitoredBlockCoords info) {
		return info.syncCoords.getCoords().equals(syncCoords.getCoords());
	}

	@Override
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof MonitoredBlockCoords;
	}

	@Override
	public String getClientIdentifier() {
		return unlocalizedName.getObject();
	}


	@Override
	public String getClientObject() {		
		return syncCoords.getCoords().toString();
	}

	@Override
	public String getClientType() {
		return "position";
	}

	public boolean equals(Object obj) {
		if (obj instanceof MonitoredBlockCoords) {
			MonitoredBlockCoords monitoredCoords = (MonitoredBlockCoords) obj;
			return monitoredCoords.syncCoords.equals(syncCoords) && monitoredCoords.unlocalizedName.equals(unlocalizedName);
		}
		return false;
	}

	@Override
	public ChannelNetworkHandler getHandler() {
		return ChannelNetworkHandler.INSTANCE;
	}

	@Override
	public boolean isValid() {
		return syncCoords.getCoords() != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public MonitoredBlockCoords copy() {
		return new MonitoredBlockCoords(syncCoords.getCoords(), unlocalizedName.getObject());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, getClientIdentifier(), getClientObject());
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		BlockCoords coords = syncCoords.getCoords();
		objects.add(new ComparableObject(this, "x", coords.getX()));
		objects.add(new ComparableObject(this, "y", coords.getY()));
		objects.add(new ComparableObject(this, "z", coords.getZ()));
		return objects;
	}
}