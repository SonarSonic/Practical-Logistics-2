package sonar.logistics.connections;

import java.util.function.Consumer;

public enum NetworkUpdate {
	CABLES(LogisticsNetwork::updateCables), //
	LOCAL(LogisticsNetwork::updateLocalChannels), //
	GLOBAL(LogisticsNetwork::updateGlobalChannels), //
	HANDLER_CHANNELS(LogisticsNetwork::updateHandlerChannels),//
	NOTIFY_WATCHING_NETWORKS(LogisticsNetwork::notifyWatchingNetworks);

	Consumer<LogisticsNetwork> updateMethod;

	NetworkUpdate(Consumer<LogisticsNetwork> updateMethod) {
		this.updateMethod = updateMethod;
	}

}
