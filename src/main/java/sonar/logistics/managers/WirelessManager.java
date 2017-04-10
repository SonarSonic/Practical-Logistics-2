package sonar.logistics.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import sonar.logistics.PL2;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.network.PacketClientEmitters;

public class WirelessManager {

	public static List<IDataEmitter> emitters = new ArrayList<IDataEmitter>();
	public static List<IDataReceiver> receivers = new ArrayList<IDataReceiver>();
	public static List<EntityPlayer> viewers = new ArrayList<EntityPlayer>();
	// client

	private static boolean dirty;

	public static void removeAll() {
		emitters.clear();
		receivers.clear();
		viewers.clear();
	}

	public static void emitterChanged(IDataEmitter emitter) {
		dirty = true;
		for (ILogisticsNetwork network : PL2.getNetworkManager().cache.values()) {
			network.onCacheChanged(CacheHandler.EMITTERS);
			network.onCacheChanged(CacheHandler.RECEIVERS);
		}
	}

	public static void receiverChanged(IDataReceiver emitter) {
		dirty = true;
		for (ILogisticsNetwork network : PL2.getNetworkManager().cache.values()) {
			network.onCacheChanged(CacheHandler.EMITTERS);
			network.onCacheChanged(CacheHandler.RECEIVERS);
		}
	}

	public static List<IDataEmitter> getEmitters(UUID uuid) {
		List<IDataEmitter> emitters = Lists.newArrayList();
		for (IDataEmitter emitter : emitters) {
			if (emitter.canPlayerConnect(uuid)) {
				emitters.add(emitter);
			}
		}
		return emitters;
	}

	public static void addEmitter(IDataEmitter emitter) {
		if (!emitters.contains(emitter)) {
			emitters.add(emitter);
			emitterChanged(emitter);
		}
	}

	public static void removeEmitter(IDataEmitter emitter) {
		if (emitters.remove(emitter)) {
			emitterChanged(emitter);
		}
	}

	public static void addReceiver(IDataReceiver receiver) {
		if (!receivers.contains(receiver)) {
			receivers.add(receiver);
			receiverChanged(receiver);
		}
	}

	public static void removeReceiver(IDataReceiver receiver) {
		if (receivers.remove(receiver)) {
			receiverChanged(receiver);
		}
	}

	public static IDataEmitter getEmitter(int identity) {
		for (IDataEmitter e : emitters) {
			if (e.getIdentity() == identity) {
				return e;
			}
		}
		return null;
	}

	public static IDataReceiver getReceiver(int identity) {
		for (IDataReceiver r : receivers) {
			if (r.getIdentity() == identity) {
				return r;
			}
		}
		return null;
	}

	public static void addViewer(EntityPlayer player) {
		if (!viewers.contains(player)) {
			viewers.add(player);
			getAndSendPacketForViewer(player);
		}
	}

	public static void removeViewer(EntityPlayer player) {
		if (viewers.contains(player)) {
			viewers.remove(player);
		}
	}

	public static void tick() {
		if (dirty) {
			viewers.forEach(player -> getAndSendPacketForViewer(player));
			dirty = false;
		}
	}

	public static void getAndSendPacketForViewer(EntityPlayer player) {
		List<IDataEmitter> emitters = getEmitters(player.getGameProfile().getId());
		ArrayList<ClientDataEmitter> clientEmitters = Lists.newArrayList();
		for (IDataEmitter emitter : WirelessManager.emitters) {
			clientEmitters.add(new ClientDataEmitter(emitter));
		}
		PL2.network.sendTo(new PacketClientEmitters(clientEmitters), (EntityPlayerMP) player);
	}

}