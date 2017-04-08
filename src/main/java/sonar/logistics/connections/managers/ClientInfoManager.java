package sonar.logistics.connections.managers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.connecting.IInfoManager;
import sonar.logistics.api.displays.ConnectedDisplayScreen;
import sonar.logistics.api.displays.ILargeDisplay;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.readers.ClientViewable;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoHelper;

public class ClientInfoManager implements IInfoManager {

	public ConcurrentHashMap<Integer, ConnectedDisplayScreen> connectedDisplays = new ConcurrentHashMap<Integer, ConnectedDisplayScreen>();

	// public LinkedHashMap<InfoUUID, IMonitorInfo> lastInfo = new LinkedHashMap();
	public LinkedHashMap<InfoUUID, IMonitorInfo> info = new LinkedHashMap();

	public Map<Integer, ArrayList<Object>> sortedLogicMonitors = new ConcurrentHashMap<Integer, ArrayList<Object>>();
	public Map<Integer, ArrayList<ClientViewable>> clientLogicMonitors = new ConcurrentHashMap<Integer, ArrayList<ClientViewable>>();

	public LinkedHashMap<InfoUUID, MonitoredList<?>> monitoredLists = new LinkedHashMap();
	public LinkedHashMap<Integer, ILogicViewable> monitors = new LinkedHashMap();
	public Map<Integer, MonitoredList<IMonitorInfo>> channelMap = new ConcurrentHashMap<Integer, MonitoredList<IMonitorInfo>>();

	// emitters
	public ArrayList<ClientDataEmitter> clientEmitters = new ArrayList<ClientDataEmitter>();

	@Override
	public void removeAll() {
		connectedDisplays.clear();
		info.clear();
		sortedLogicMonitors.clear();
		clientLogicMonitors.clear();
		monitoredLists.clear();
		monitors.clear();
		channelMap.clear();
		clientEmitters.clear();
	}

	public void onInfoPacket(NBTTagCompound packetTag, SyncType type) {
		NBTTagList packetList = packetTag.getTagList("infoList", NBT.TAG_COMPOUND);
		boolean save = type.isType(SyncType.SAVE);
		for (int i = 0; i < packetList.tagCount(); i++) {
			NBTTagCompound infoTag = packetList.getCompoundTagAt(i);
			InfoUUID id = NBTHelper.instanceNBTSyncable(InfoUUID.class, infoTag);
			if (save) {
				info.put(id, InfoHelper.readInfoFromNBT(infoTag));
				// info.replace(id, );
			} else {
				IMonitorInfo currentInfo = info.get(id);
				if (currentInfo != null) {
					currentInfo.readData(infoTag, type);
					info.put(id, currentInfo);
				}
			}
		}
	}

	public void addInfoProvider(IInfoProvider infoProvider) {
		if (monitors.containsValue(infoProvider) || infoProvider.getIdentity() == -1) {
			return;
		}
		monitors.put(infoProvider.getIdentity(), infoProvider);
	}

	public void removeInfoProvider(IInfoProvider monitor) {
		monitors.remove(monitor.getIdentity());
	}

	@Override
	public LinkedHashMap<Integer, ILogicViewable> getMonitors() {
		return monitors;
	}

	@Override
	public LinkedHashMap<InfoUUID, IMonitorInfo> getInfoList() {
		return info;
	}

	public <T extends IMonitorInfo> MonitoredList<T> getMonitoredList(int networkID, InfoUUID uuid) {
		MonitoredList<T> list = MonitoredList.<T>newMonitoredList(networkID);
		monitoredLists.putIfAbsent(uuid, list);
		for (Entry<InfoUUID, MonitoredList<?>> entry : monitoredLists.entrySet()) {
			if (entry.getValue().networkID == networkID && entry.getKey().equals(uuid)) {
				return (MonitoredList<T>) entry.getValue();
			}
		}
		return list;
	}

	public ConnectedDisplayScreen getOrCreateDisplayScreen(World world, ILargeDisplay display, int registryID) {
		ConcurrentHashMap<Integer, ConnectedDisplayScreen> displays = getConnectedDisplays();
		ConnectedDisplayScreen toSet = displays.get(registryID);
		if (toSet == null) {
			displays.put(registryID, new ConnectedDisplayScreen(display));
			toSet = displays.get(registryID);
		}
		return toSet;
	}

	@Override
	public ConcurrentHashMap<Integer, ConnectedDisplayScreen> getConnectedDisplays() {
		return connectedDisplays;
	}
}
