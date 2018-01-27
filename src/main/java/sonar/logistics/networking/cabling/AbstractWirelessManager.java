package sonar.logistics.networking.cabling;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.wireless.ClientWirelessEmitter;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.api.wireless.IWirelessEmitter;
import sonar.logistics.api.wireless.IWirelessManager;
import sonar.logistics.api.wireless.IWirelessReceiver;
import sonar.logistics.api.wireless.WirelessSecurity;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.CacheHandler;

public abstract class AbstractWirelessManager<N, E extends IWirelessEmitter, R extends IWirelessReceiver> implements IWirelessManager<E, R> {

	/** a cache of all Data Emitters which currently belong to a network */
	public List<E> emitters = new ArrayList<E>();
	/** a cache of all Data Receivers which currently belong to a network */
	public List<R> receivers = new ArrayList<R>();
	/// ** players which are currently viewing the selection menu in the {@link IDataReceiver}'s GUI */
	public ListenableList<PlayerListener> player_viewers = new ListenableList(this, 1);

	/** used to mark if new viewers have been added, which will require the latest packet */
	protected boolean dirty;

	public void removeAll() {
		emitters.clear();
		receivers.clear();
	}

	//// CONNECTION EVENTS \\\\

	public abstract void onReceiverConnected(N main, R receiver);	

	public abstract void onReceiverDisconnected(N network, R receiver);
	
	/** called by the {@link CacheHandler} when a {@link IDataReceiver} is connected to a network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataReceiver} is connected to
	 * @param receiver the {@link IDataReceiver} which has been connected */
	public void connectReceiver(N network, R receiver) {
		if (!receivers.contains(receiver)) {
			receivers.add(receiver);
			onReceiverConnected(network, receiver);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataReceiver} is disconnected from network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataReceiver} has disconnected from
	 * @param receiver the {@link IDataReceiver} which has been disconnected */
	public void disconnectReceiver(N network, R receiver) {
		if (receivers.remove(receiver)) {
			onReceiverDisconnected(network, receiver);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataEmitter} is connected to a network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataEmitter} is connected to
	 * @param emitter the {@link IDataEmitter} which has been connected */
	public void connectEmitter(N network, E emitter) {
		if (!emitters.contains(emitter)) {
			emitters.add(emitter);
			onEmitterConnected(network, emitter);
		}
	}

	/** called by the {@link CacheHandler} when a {@link IDataEmitter} is disconnected from network
	 * @param network the {@link ILogisticsNetwork} the {@link IDataEmitter} has disconnected from
	 * @param emitter the {@link IDataEmitter} which has been disconnected */
	public void disconnectEmitter(N network, E emitter) {
		if (emitters.remove(emitter)) {
			onEmitterDisconnected(network, emitter);
		}
	}

	/** connects a {@link IDataEmitter} to a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataEmitter}'s network
	 * @param emitter the {@link IDataEmitter} which has been connected */
	public void onEmitterConnected(N network, E emitter) {
		receivers.forEach(receiver -> {
			if (receiver.canAccess(emitter).isConnected()) {
				receiver.onEmitterConnected(emitter);
			}
		});
	}

	/** disconnects a {@link IDataEmitter} from a {@link ILogisticsNetwork}'s
	 * @param main the {@link IDataEmitter}'s network
	 * @param emitter the {@link IDataEmitter} which has been disconnected */
	public void onEmitterDisconnected(N network, E emitter) {
		receivers.forEach(receiver -> {
			if (receiver.canAccess(emitter).isConnected()) {
				receiver.onEmitterDisconnected(emitter);
			}
		});
	}

	/** alerts all connected {@link IDataReceiver}s of a {@link IDataEmitter}'s security change
	 * @param emitter the {@link IDataEmitter} which has had it's security changed
	 * @param oldSetting the original {@link IDataEmitter}'s security setting */
	public void onEmitterSecurityChanged(E emitter, WirelessSecurity oldSetting) {
		receivers.forEach(receiver -> {
			if (receiver.canEmitterAccessReceiver(emitter).isConnected())
				receiver.onEmitterSecurityChanged(emitter, oldSetting);
		});
		dirty = true; // updates packets of viewable emitters
	}

	//// HELPER METHODS \\\\

	public List<E> getEmitters(UUID uuid) {
		List<E> list = Lists.newArrayList();
		for (E emitter : emitters) {
			if (emitter.canPlayerConnect(uuid).isConnected()) {
				list.add(emitter);
			}
		}
		return list;
	}

	/** returns a {@link IDataEmitter} with a matching unique identity */
	public E getEmitter(int identity) {
		for (E e : emitters) {
			if (e.getIdentity() == identity) {
				return e;
			}
		}
		return null;
	}

	/** returns a {@link IDataReceiver} with a matching unique identity */
	public R getReceiver(int identity) {
		for (R r : receivers) {
			if (r.getIdentity() == identity) {
				return r;
			}
		}
		return null;
	}

	//// UPDATE TICK \\\\

	public void tick() {
		if (dirty) {
			player_viewers.forEach(player -> PacketHelper.sendEmittersToPlayer(player.listener.player, this));
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
	
	
	public ArrayList<ClientWirelessEmitter> getClientEmitters(EntityPlayer player) {
		List<E> emitters = getEmitters(player.getGameProfile().getId());
		ArrayList<ClientWirelessEmitter> clientEmitters = Lists.newArrayList();
		for (E emitter : emitters) {
			clientEmitters.add(new ClientWirelessEmitter(emitter));
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
		PacketHelper.sendEmittersToPlayer(tally.listener.player, this);
	}

	@Override
	public void onListenerRemoved(ListenerTally<PlayerListener> tally) {}

	@Override
	public void onSubListenableAdded(ISonarListenable<PlayerListener> listen) {}

	@Override
	public void onSubListenableRemoved(ISonarListenable<PlayerListener> listen) {}
}
