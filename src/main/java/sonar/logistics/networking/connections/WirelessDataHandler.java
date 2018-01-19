package sonar.logistics.networking.connections;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.logistics.PL2;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.api.wireless.DataEmitterSecurity;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.CacheHandler;
import sonar.logistics.networking.NetworkUpdate;

public class WirelessDataHandler implements ISonarListenable<PlayerListener> {

	/** a cache of all Data Emitters which currently belong to a network */
	public List<IDataEmitter> data_emitters = new ArrayList<IDataEmitter>();
	/** a cache of all Data Receivers which currently belong to a network */
	public List<IDataReceiver> data_receivers = new ArrayList<IDataReceiver>();
	/// ** players which are currently viewing the selection menu in the {@link IDataReceiver}'s GUI */
	public ListenableList<PlayerListener> player_viewers = new ListenableList(this, 1);

	/** used to mark if new viewers have been added, which will require the latest packet */
	private boolean dirty;

	public void removeAll() {
		data_emitters.clear();
		data_receivers.clear();
	}

	/** connects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s network can read the {@link IDataEmitter}'s network
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the Data Emitter's Network (which is connected to by the receivers network) */
	public void connectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().addListener(connected, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().addListener(watcher, ILogisticsNetwork.WATCHING_NETWORK);
		watcher.markUpdate(NetworkUpdate.GLOBAL, NetworkUpdate.NOTIFY_WATCHING_NETWORKS);
	}

	/** disconnects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s network can no longer read the {@link IDataEmitter}'s network, however if multiple receivers/emitters between the two networks exist the networks will remain connected
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the {@link IDataEmitters}'s Network (which is connected to by the receivers network) */
	public void disconnectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().removeListener(connected, true, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().removeListener(watcher, true, ILogisticsNetwork.WATCHING_NETWORK);
		watcher.markUpdate(NetworkUpdate.GLOBAL, NetworkUpdate.NOTIFY_WATCHING_NETWORKS);
	}

	//// CONNECTION EVENTS \\\\

	/** called by the {@link CacheHandler} when a {@link IDataReceiver} is connected to a network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataReceiver} is connected to
	 * @param emitter the {@link IDataReceiver} which has been connected */
	public void connectDataReceiver(ILogisticsNetwork network, IDataReceiver receiver) {
		if (!data_receivers.contains(receiver)) {
			data_receivers.add(receiver);
			receiver.refreshConnectedNetworks();
			onDataReceiverConnected(network, receiver);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataReceiver} is disconnected from network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataReceiver} has disconnected from
	 * @param emitter the {@link IDataReceiver} which has been disconnected */
	public void disconnectDataReceiver(ILogisticsNetwork network, IDataReceiver receiver) {
		if (data_receivers.remove(receiver)) {
			onDataReceiverDisconnected(network, receiver);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataEmitter} is connected to a network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataEmitter} is connected to
	 * @param emitter the {@link IDataEmitter} which has been connected */
	public void connectDataEmitter(ILogisticsNetwork network, IDataEmitter emitter) {
		if (!data_emitters.contains(emitter)) {
			data_emitters.add(emitter);
			onDataEmitterConnected(network, emitter);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataEmitter} is disconnected from network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataEmitter} has disconnected from
	 * @param emitter the {@link IDataEmitter} which has been disconnected */
	public void disconnectDataEmitter(ILogisticsNetwork network, IDataEmitter emitter) {
		if (data_emitters.remove(emitter)) {
			onDataEmitterDisconnected(network, emitter);
		}
	}

	/** connects a {@link IDataReceiver} to a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataReceiver}'s network
	 * @param receiver the {@link IDataReceiver} which has been connected */
	public void onDataReceiverConnected(ILogisticsNetwork main, IDataReceiver receiver) {
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = PL2.getNetworkManager().getNetwork(networkID);
			if (sub.getNetworkID() != main.getNetworkID() && sub.isValid()) {
				connectNetworks(main, sub);
			}
		});
	}

	/** disconnects a {@link IDataReceiver} from a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataReceiver}'s network
	 * @param receiver the {@link IDataReceiver} which has been disconnected */
	public void onDataReceiverDisconnected(ILogisticsNetwork network, IDataReceiver receiver) {
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = PL2.getNetworkManager().getNetwork(networkID);
			if (sub.getNetworkID() != network.getNetworkID() && sub.isValid()) {
				disconnectNetworks(network, sub);
			}
		});
	}

	/** connects a {@link IDataEmitter} to a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataEmitter}'s network
	 * @param emitter the {@link IDataEmitter} which has been connected */
	public void onDataEmitterConnected(ILogisticsNetwork network, IDataEmitter emitter) {
		data_receivers.forEach(receiver -> {
			if (receiver.canAccess(emitter)) {
				receiver.onEmitterConnected(emitter);
				connectNetworks(receiver.getNetwork(), network);
			}
		});
	}

	/** disconnects a {@link IDataEmitter} from a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataEmitter}'s network
	 * @param emitter the {@link IDataEmitter} which has been disconnected */
	public void onDataEmitterDisconnected(ILogisticsNetwork network, IDataEmitter emitter) {
		data_receivers.forEach(receiver -> {
			if (receiver.canAccess(emitter)) {
				receiver.onEmitterDisconnected(emitter);
				disconnectNetworks(receiver.getNetwork(), network);
			}
		});
	}

	/** alerts all connected {@link IDataReceiver}s of a {@link IDataEmitter}'s security change
	 * @param emitter the {@link IDataEmitter} which has had it's security changed
	 * @param oldSetting the original {@link IDataEmitter}'s security setting */
	public void onEmitterSecurityChanged(IDataEmitter emitter, DataEmitterSecurity oldSetting) {
		data_receivers.forEach(receiver -> {
			if (receiver.canEmitterAccessReceiver(emitter))
				receiver.onEmitterSecurityChanged(emitter, oldSetting);
		});
		dirty = true; // updates packets of viewable emitters
	}

	//// HELPER METHODS \\\\

	public List<IDataEmitter> getEmitters(UUID uuid) {
		List<IDataEmitter> emitters = Lists.newArrayList();
		for (IDataEmitter emitter : emitters) {
			if (emitter.canPlayerConnect(uuid)) {
				emitters.add(emitter);
			}
		}
		return emitters;
	}

	/** returns a {@link IDataEmitter} with a matching unique identity */
	public IDataEmitter getDataEmitter(int identity) {
		for (IDataEmitter e : data_emitters) {
			if (e.getIdentity() == identity) {
				return e;
			}
		}
		return null;
	}

	/** returns a {@link IDataReceiver} with a matching unique identity */
	public IDataReceiver getDataReceiver(int identity) {
		for (IDataReceiver r : data_receivers) {
			if (r.getIdentity() == identity) {
				return r;
			}
		}
		return null;
	}

	//// UPDATE TICK \\\\

	public void tick() {
		if (dirty) {
			player_viewers.forEach(player -> PacketHelper.sendDataEmittersToPlayer(player.listener.player));
			dirty = false;
		}
	}

	//// PLAYER VIEWERS \\\\
	
	
	public void addViewer(EntityPlayer player) {
		player_viewers.addListener(player, 0);
	}

	public void removeViewer(EntityPlayer player) {
		player_viewers.removeListener(player, true, 0);
	}
	
	
	public ArrayList<ClientDataEmitter> getClientDataEmitters(EntityPlayer player) {
		List<IDataEmitter> emitters = getEmitters(player.getGameProfile().getId());
		ArrayList<ClientDataEmitter> clientEmitters = Lists.newArrayList();
		for (IDataEmitter emitter : data_emitters) {
			clientEmitters.add(new ClientDataEmitter(emitter));
		}
		return clientEmitters;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public ListenableList<PlayerListener> getListenerList() {
		return player_viewers;
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally) {
		PacketHelper.sendDataEmittersToPlayer(tally.listener.player);
	}

	@Override
	public void onListenerRemoved(ListenerTally<PlayerListener> tally) {}

	@Override
	public void onSubListenableAdded(ISonarListenable<PlayerListener> listen) {}

	@Override
	public void onSubListenableRemoved(ISonarListenable<PlayerListener> listen) {}

}