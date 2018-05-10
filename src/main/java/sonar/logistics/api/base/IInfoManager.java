package sonar.logistics.api.base;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;

import java.util.Map;

public interface IInfoManager {

	Map<InfoUUID, IInfo> getInfoMap();

	Map<Integer, DisplayGSI> getGSIMap();

	Map<Integer, ILogicListenable> getNetworkTileMap();

	Map<Integer, ConnectedDisplay> getConnectedDisplays();

	Map<InfoUUID, AbstractChangeableList> getChangeableListMap();

	void setInfo(InfoUUID uuid, IInfo newInfo);

	void addIdentityTile(ILogicListenable infoProvider, PL2AdditionType type);

	void removeIdentityTile(ILogicListenable monitor, PL2RemovalType type);

	void removeAll();
	
}
