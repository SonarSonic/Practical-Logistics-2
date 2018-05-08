package sonar.logistics.api.core.tiles.readers;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.listeners.ILogicListenable;

import java.util.List;
import java.util.Map;

public interface IListReader<T extends IInfo> extends ILogicListenable {

	AbstractChangeableList<T> sortMonitoredList(AbstractChangeableList<T> updateInfo, int channelID);	
	
	AbstractChangeableList<T> getViewableList(AbstractChangeableList<T> updateList, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<T>> channels, List<NodeConnection> usedChannels);	

	List<NodeConnection> getUsedChannels(Map<NodeConnection, AbstractChangeableList<T>> channels);
	
	List<INetworkHandler> getValidHandlers();
}
