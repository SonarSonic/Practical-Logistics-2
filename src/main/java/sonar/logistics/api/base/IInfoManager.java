package sonar.logistics.api.base;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;

import javax.annotation.Nullable;
import java.util.Map;

public interface IInfoManager {

	Map<Integer,ILogicListenable> getMonitors();

	Map<InfoUUID, IInfo> getInfoList();

	IInfo getInfoFromUUID(InfoUUID uuid);
	
	void setInfo(InfoUUID uuid, IInfo newInfo);

	Map<Integer, ConnectedDisplay> getConnectedDisplays();

	@Nullable
    <T extends IInfo> AbstractChangeableList getMonitoredList(InfoUUID uuid);

	ConnectedDisplay getConnectedDisplay(int iden);

	void addIdentityTile(ILogicListenable infoProvider, PL2AdditionType type);

	void removeIdentityTile(ILogicListenable monitor, PL2RemovalType type);

	void removeAll();
	
	DisplayGSI getGSI(int iden);

	void addDisplay(IDisplay display, PL2AdditionType type);

	void removeDisplay(IDisplay display, PL2RemovalType type);
	
}
