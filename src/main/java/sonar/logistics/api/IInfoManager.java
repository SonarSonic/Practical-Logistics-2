package sonar.logistics.api;

import java.util.Map;

import javax.annotation.Nullable;

import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.api.utils.PL2RemovalType;
import sonar.logistics.api.viewers.ILogicListenable;

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
