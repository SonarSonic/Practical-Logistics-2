package sonar.logistics.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import sonar.logistics.PL2;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.api.wireless.DataEmitterSecurity;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.connections.NetworkUpdate;
import sonar.logistics.helpers.PacketHelper;

public class WirelessManager {

	/** a cache of all Data Emitters which currently belong to a network */
	public static List<IDataEmitter> data_emitters = new ArrayList<IDataEmitter>();
	/** a cache of all Data Receivers which currently belong to a network */
	public static List<IDataReceiver> data_receivers = new ArrayList<IDataReceiver>();
	/** players which are currently viewing the selection menu in the {@link IDataReceiver}'s GUI */
	public static List<EntityPlayer> player_viewers = new ArrayList<EntityPlayer>();

	/** used to mark if new viewers have been added, which will require the latest packet */
	private static boolean dirty;

	public static void removeAll() {
		data_emitters.clear();
		data_receivers.clear();
		player_viewers.clear();
	}

	/** connects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s network can read the {@link IDataEmitter}'s network
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the Data Emitter's Network (which is connected to by the receivers network) */
	public static void connectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().addListener(connected, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().addListener(watcher, ILogisticsNetwork.WATCHING_NETWORK);	
		watcher.markUpdate(NetworkUpdate.GLOBAL, NetworkUpdate.NOTIFY_WATCHING_NETWORKS);
	}

	/** disconnects two {@link ILogisticsNetwork}'s so the {@link IDataReceiver}'s network can no longer read the {@link IDataEmitter}'s network, however if multiple receivers/emitters between the two networks exist the networks will remain connected
	 * @param watcher the {@link IDataReceiver}'s Network (which watches the emitters network)
	 * @param connected the {@link IDataEmitters}'s Network (which is connected to by the receivers network) */
	public static void disconnectNetworks(ILogisticsNetwork watcher, ILogisticsNetwork connected) {
		watcher.getListenerList().removeListener(connected, true, ILogisticsNetwork.CONNECTED_NETWORK);
		connected.getListenerList().removeListener(watcher, true, ILogisticsNetwork.WATCHING_NETWORK);		
		watcher.markUpdate(NetworkUpdate.GLOBAL, NetworkUpdate.NOTIFY_WATCHING_NETWORKS);
	}

	//// CONNECTION EVENTS \\\\

	/** called by the {@link CacheHandler} when a {@link IDataReceiver} is connected to a network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataReceiver} is connected to
	 * @param emitter the {@link IDataReceiver} which has been connected */
	public static void connectDataReceiver(ILogisticsNetwork network, IDataReceiver receiver) {
		if (!data_receivers.contains(receiver)) {
			data_receivers.add(receiver);
			receiver.refreshConnectedNetworks();
			onDataReceiverConnected(network, receiver);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataReceiver} is disconnected from network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataReceiver} has disconnected from
	 * @param emitter the {@link IDataReceiver} which has been disconnected */
	public static void disconnectDataReceiver(ILogisticsNetwork network, IDataReceiver receiver) {
		if (data_receivers.remove(receiver)) {
			onDataReceiverDisconnected(network, receiver);
		}
	}
	
	/** called by the {@link CacheHandler} when a {@link IDataEmitter} is connected to a network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataEmitter} is connected to
	 * @param emitter the {@link IDataEmitter} which has been connected */
	public static void connectDataEmitter(ILogisticsNetwork network, IDataEmitter emitter) {
		if (!data_emitters.contains(emitter)) {
			data_emitters.add(emitter);
			onDataEmitterConnected(network, emitter);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataEmitter} is disconnected from network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataEmitter} has disconnected from
	 * @param emitter the {@link IDataEmitter} which has been disconnected */
	public static void disconnectDataEmitter(ILogisticsNetwork network, IDataEmitter emitter) {
		if (data_emitters.remove(emitter)) {
			onDataEmitterDisconnected(network, emitter);
		}
	}

	/** connects a {@link IDataReceiver} to a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataReceiver}'s network
	 * @param receiver the {@link IDataReceiver} which has been connected */
	public static void onDataReceiverConnected(ILogisticsNetwork main, IDataReceiver receiver) {
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
	public static void onDataReceiverDisconnected(ILogisticsNetwork network, IDataReceiver receiver) {
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
	public static void onDataEmitterConnected(ILogisticsNetwork network, IDataEmitter emitter) {
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
	public static void onDataEmitterDisconnected(ILogisticsNetwork network, IDataEmitter emitter) {
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
	public static void onEmitterSecurityChanged(IDataEmitter emitter, DataEmitterSecurity oldSetting) {
		data_receivers.forEach(receiver -> {
			if (receiver.canEmitterAccessReceiver(emitter))
				receiver.onEmitterSecurityChanged(emitter, oldSetting);
		});
		dirty = true; // updates packets of viewable emitters
	}

	//// HELPER METHODS \\\\

	public static List<IDataEmitter> getEmitters(UUID uuid) {
		List<IDataEmitter> emitters = Lists.newArrayList();
		for (IDataEmitter emitter : emitters) {
			if (emitter.canPlayerConnect(uuid)) {
				emitters.add(emitter);
			}
		}
		return emitters;
	}

	/**returns a {@link IDataEmitter} with a matching unique identity*/
	public static IDataEmitter getDataEmitter(int identity) {
		for (IDataEmitter e : data_emitters) {
			if (e.getIdentity() == identity) {
				return e;
			}
		}
		return null;
	}

	/**returns a {@link IDataReceiver} with a matching unique identity*/
	public static IDataReceiver getDataReceiver(int identity) {
		for (IDataReceiver r : data_receivers) {
			if (r.getIdentity() == identity) {
				return r;
			}
		}
		return null;
	}

	//// UPDATE TICK \\\\

	public static void tick() {
		if (dirty) {
			player_viewers.forEach(player -> PacketHelper.sendDataEmittersToPlayer(player));
			dirty = false;
		}
	}

	//// PLAYER VIEWERS \\\\

	public static void addViewer(EntityPlayer player) {
		if (!player_viewers.contains(player)) {
			player_viewers.add(player);
			PacketHelper.sendDataEmittersToPlayer(player);
		}
	}

	public static void removeViewer(EntityPlayer player) {
		if (player_viewers.contains(player)) {
			player_viewers.remove(player);
		}
	}

	public static ArrayList<ClientDataEmitter> getClientDataEmitters(EntityPlayer player) {
		List<IDataEmitter> emitters = getEmitters(player.getGameProfile().getId());
		ArrayList<ClientDataEmitter> clientEmitters = Lists.newArrayList();
		for (IDataEmitter emitter : WirelessManager.data_emitters) {
			clientEmitters.add(new ClientDataEmitter(emitter));
		}
		return clientEmitters;
	}

}