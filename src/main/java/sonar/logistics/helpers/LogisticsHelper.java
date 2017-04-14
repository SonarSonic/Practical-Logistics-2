package sonar.logistics.helpers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenerList;
import sonar.core.listener.PlayerListener;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.IDataReceiver;
import sonar.logistics.api.wireless.IEntityTransceiver;
import sonar.logistics.api.wireless.ITileTransceiver;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.network.PacketChannels;
import sonar.logistics.network.PacketMonitoredList;

public class LogisticsHelper {

	public static boolean isPlayerUsingOperator(EntityPlayer player) {
		if (player.getHeldItemMainhand() != null) {
			return player.getHeldItemMainhand().getItem() instanceof IOperatorTool;
		}
		return false;
	}

	public static List<ILogisticsNetwork> getNetworks(List<Integer> ids) {
		List<ILogisticsNetwork> networks = Lists.newArrayList();
		ids.forEach(id -> PL2.getNetworkManager().getNetwork(id));
		return networks;
	}

	public static Map<CacheHandler, List> getCachesMap() {
		Map<CacheHandler, List> connections = Maps.newHashMap();
		CacheHandler.handlers.forEach(classType -> connections.put(classType, Lists.newArrayList()));
		return connections;
	}

	public static NodeConnection getTransceiverNode(INetworkTile source, ItemStack stack) {
		if (stack.getItem() instanceof ITileTransceiver) {
			ITileTransceiver trans = (ITileTransceiver) stack.getItem();
			return new BlockConnection(source, trans.getCoords(stack), trans.getDirection(stack));
		}
		if (stack.getItem() instanceof IEntityTransceiver) {
			IEntityTransceiver trans = (IEntityTransceiver) stack.getItem();
			UUID uuid = trans.getEntityUUID(stack);
			if (uuid != null) {
				for (Entity entity : source.getCoords().getWorld().getLoadedEntityList()) {
					if (entity.getPersistentID().equals(uuid)) {
						return new EntityConnection(source, entity);
					}
				}
			}
		}
		return null;

	}

	public static void addConnectedNetworks(ILogisticsNetwork main, IDataReceiver receiver) {
		List<Integer> connected = receiver.getConnectedNetworks();
		connected.iterator().forEachRemaining(networkID -> {
			ILogisticsNetwork sub = PL2.getNetworkManager().getNetwork(networkID);
			if (sub.getNetworkID() != main.getNetworkID() && sub.isValid()) {
				sub.getListenerList().addListener(main, ILogisticsNetwork.WATCHING_NETWORK);
			}
		});
	}

	public static List<NodeConnection> sortNodeConnections(List<NodeConnection> channels, List<INode> nodes) {
		nodes.forEach(n -> {
			if (n.isValid())
				n.addConnections(channels);
		});
		return NodeConnection.sortConnections(channels);
	}

	public static void sendNormalProviderInfo(IInfoProvider monitor) {
		LogisticsHelper.sendPacketsToListeners(monitor, null, null, new InfoUUID(monitor.getIdentity(), 0));
	}

	public static void sendFullInfo(List<PlayerListener> listeners, ILogicListenable monitor, MonitoredList saveList, InfoUUID uuid) {
		NBTTagCompound saveTag = saveList != null ? InfoHelper.writeMonitoredList(new NBTTagCompound(), true, saveList, SyncType.DEFAULT_SYNC) : null;
		if (saveTag.hasNoTags())
			return;
		listeners.forEach(listener -> PL2.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), uuid, monitor.getNetworkID(), saveTag, SyncType.DEFAULT_SYNC), listener.player));
	}

	public static void sendPacketsToListeners(ILogicListenable reader, MonitoredList saveList, MonitoredList lastList, InfoUUID uuid) {
		ListenerList<PlayerListener> list = reader.getListenerList();
		types: for (ListenerType type : ListenerType.ALL) {
			List<PlayerListener> listeners = list.getListeners(type);
			if (listeners.isEmpty()) {
				continue types;
			}
			//TODO why isn't Fluid Reader connecting?
			switch (type) {
			case FULL_INFO:
				if (saveList != null) {
					NBTTagCompound saveTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), true, saveList, SyncType.DEFAULT_SYNC);
					if (saveTag == null || saveTag.hasNoTags())
						continue types;
					listeners.forEach(listener -> {
						PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), uuid, reader.getNetworkID(), saveTag, SyncType.DEFAULT_SYNC), listener.player);
						list.removeListener(listener, ListenerType.FULL_INFO);
						list.addListener(listener, ListenerType.INFO);

					});
				}
				continue types;

			case INFO:
				if (saveList == null) {
					continue types;
				}
				NBTTagCompound tag = InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.SPECIAL);
				if (tag.hasNoTags() || (saveList.changed.isEmpty() && saveList.removed.isEmpty())) {
					continue types;
				}
				listeners.forEach(listener -> PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), uuid, reader.getNetworkID(), tag, SyncType.SPECIAL), listener.player));
				break;
			case TEMPORARY:
				if (saveList != null) {
					NBTTagCompound saveTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), lastList.isEmpty(), saveList, SyncType.DEFAULT_SYNC);
					NBTTagList tagList = new NBTTagList();
					if (reader instanceof INetworkReader) {
						INetworkReader r = (INetworkReader) reader;
						for (int i = 0; i < r.getMaxInfo(); i++) {
							InfoUUID infoID = new InfoUUID(reader.getIdentity(), i);
							IInfo info = PL2.getServerManager().info.get(infoID);
							if (info != null) {
								NBTTagCompound nbt = InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE);
								nbt = infoID.writeData(nbt, SyncType.SAVE);
								tagList.appendTag(nbt);
							}
						}
					}
					listeners.forEach(listener -> {
						PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), uuid, saveList.networkID, saveTag, SyncType.DEFAULT_SYNC), listener.player);
						list.removeListener(listener, ListenerType.TEMPORARY); // remove from source not from
						PL2.getServerManager().sendPlayerPacket(listener, tagList, SyncType.SAVE);
					});
				}
				continue types;
			default:
				continue types;
			}
		}
	}
}
