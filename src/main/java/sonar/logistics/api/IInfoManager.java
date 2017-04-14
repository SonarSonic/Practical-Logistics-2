package sonar.logistics.api;

import java.util.Map;

import net.minecraft.world.World;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;

public interface IInfoManager {

	public Map<Integer,ILogicListenable> getMonitors();

	public Map<InfoUUID, IInfo> getInfoList();

	public Map<Integer, ConnectedDisplay> getConnectedDisplays();

	public <T extends IInfo> MonitoredList<T> getMonitoredList(int networkID, InfoUUID uuid);

	public ConnectedDisplay getOrCreateDisplayScreen(World world, ILargeDisplay display, int registryID);

	public void addIdentityTile(ILogicListenable infoProvider);

	public void removeIdentityTile(ILogicListenable monitor);

	public void removeAll();

	public void addDisplay(IDisplay display);

	public void removeDisplay(IDisplay display);
}
