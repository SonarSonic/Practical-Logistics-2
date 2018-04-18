package sonar.logistics.api;

import java.util.Map;

import javax.annotation.Nullable;

import sonar.core.api.utils.TileAdditionType;
import sonar.core.api.utils.TileRemovalType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.viewers.ILogicListenable;

public interface IInfoManager {

	public Map<Integer,ILogicListenable> getMonitors();

	public Map<InfoUUID, IInfo> getInfoList();	

	public IInfo getInfoFromUUID(InfoUUID uuid);
	
	public void setInfo(InfoUUID uuid, IInfo newInfo);

	public Map<Integer, ConnectedDisplay> getConnectedDisplays();

	@Nullable
	public <T extends IInfo> AbstractChangeableList getMonitoredList(InfoUUID uuid);

	public ConnectedDisplay getConnectedDisplay(int iden);

	public void addIdentityTile(ILogicListenable infoProvider, TileAdditionType type);

	public void removeIdentityTile(ILogicListenable monitor, TileRemovalType type);

	public void removeAll();
	
	public DisplayGSI getGSI(int iden);

	public void addDisplay(IDisplay display);

	public void removeDisplay(IDisplay display);
	
	
}
