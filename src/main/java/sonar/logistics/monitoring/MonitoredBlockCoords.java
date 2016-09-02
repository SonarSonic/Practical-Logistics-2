package sonar.logistics.monitoring;

import com.google.common.collect.Lists;

import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.display.DisplayType;
import sonar.logistics.api.info.BaseInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.monitor.IMonitorInfo;
import sonar.logistics.api.info.monitor.LogicMonitorHandler;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = MonitoredBlockCoords.id, modid = Logistics.MODID)
public class MonitoredBlockCoords extends BaseInfo<MonitoredBlockCoords> implements INameableInfo<MonitoredBlockCoords> {

	public static final String id = "coords";
	public static LogicMonitorHandler<MonitoredBlockCoords> handler = LogicMonitorHandler.instance(ChannelMonitorHandler.id);
	public SyncCoords syncCoords = new SyncCoords(1);
	public SyncTagType.STRING unlocalizedName = new SyncTagType.STRING(2);
	{
		syncParts.addAll(Lists.newArrayList(syncCoords, unlocalizedName));
	}

	public MonitoredBlockCoords() {
	}

	public MonitoredBlockCoords(BlockCoords coords, String unlocalizedName) {
		this.syncCoords.setCoords(coords);
		this.unlocalizedName.setObject(unlocalizedName);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredBlockCoords info) {
		return info.syncCoords.equals(syncCoords);
	}

	@Override
	public boolean isMatchingInfo(MonitoredBlockCoords info) {
		return true;
	}

	@Override
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof MonitoredBlockCoords;
	}

	@Override
	public String getClientIdentifier() {
		return FontHelper.translate(unlocalizedName + ".name");
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
	public LogicMonitorHandler<MonitoredBlockCoords> getHandler() {
		return handler;
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
	public void renderInfo(DisplayType displayType, double width, double height, double scale, int infoPos) {
		InfoRenderer.renderNormalInfo(displayType, width, height, scale, getClientIdentifier(), getClientObject());

	}

}
