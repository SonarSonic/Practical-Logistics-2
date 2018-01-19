package sonar.logistics.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.ItemChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.ClientLocalProvider;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.misc.TileRedstoneSignaller;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.networking.channels.ItemNetworkChannels;
import sonar.logistics.networking.connections.WirelessDataHandler;
import sonar.logistics.packets.PacketClientEmitters;
import sonar.logistics.packets.PacketInfoUpdates;
import sonar.logistics.packets.PacketLocalProviders;
import sonar.logistics.packets.PacketMonitoredList;

public class PacketHelper {

	public static void sendLocalProvidersFromScreen(TileAbstractDisplay part, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		List<ILogicListenable> providers = new ArrayList<ILogicListenable>();
		int identity = part.getIdentity();
		if (part instanceof ILargeDisplay) {
			ConnectedDisplay display = ((ILargeDisplay) part).getDisplayScreen();
			if (display != null && display.getTopLeftScreen() != null) {
				identity = ((TileAbstractDisplay) display.getTopLeftScreen()).getIdentity();
			}
			providers = display != null ? display.getLocalProviders(providers) : LogisticsHelper.getLocalProviders(providers, world, pos, part);
		} else {
			providers = LogisticsHelper.getLocalProviders(providers, world, pos, part);
		}

		List<ClientLocalProvider> clientMonitors = Lists.newArrayList();
		providers.forEach(provider -> {
			provider.getListenerList().addListener(player, ListenerType.TEMP_LISTENER);
			clientMonitors.add(new ClientLocalProvider(provider));
		});
		PL2.network.sendTo(new PacketLocalProviders(clientMonitors, identity), (EntityPlayerMP) player);
	}

	public static void sendLocalProviders(TileRedstoneSignaller tileRedstoneSignaller, int identity, EntityPlayer player) {
		List<IInfoProvider> providers = tileRedstoneSignaller.getNetwork().getLocalInfoProviders();
		List<ClientLocalProvider> clientProviders = Lists.newArrayList();
		providers.forEach(provider -> {
			provider.getListenerList().addListener(player, ListenerType.TEMP_LISTENER);
			clientProviders.add(new ClientLocalProvider(provider));
		});
		PL2.network.sendTo(new PacketLocalProviders(clientProviders, identity), (EntityPlayerMP) player);
	}

	public static void addInfoUpdatesToList(Map<EntityPlayerMP, NBTTagList> listenerPackets, List<EntityPlayerMP> list2, NBTTagCompound updateTag, NBTTagCompound saveTag, boolean fullPacket) {
		for (EntityPlayerMP listener : list2) {
			NBTTagList list = listenerPackets.get(listener);
			if (list == null) {
				listenerPackets.put(listener, new NBTTagList());
				list = listenerPackets.get(listener);
			}
			list.appendTag(fullPacket ? saveTag.copy() : updateTag.copy());
		}
	}

	public static void sendInfoUpdatePacket(EntityPlayerMP player, NBTTagList list, SyncType type) {
		if (list.hasNoTags()) {
			return;
		}
		NBTTagCompound packetTag = new NBTTagCompound();
		packetTag.setTag("infoList", list);
		PL2.network.sendTo(new PacketInfoUpdates(packetTag, type), player);
	}

	public static void receiveInfoUpdate(NBTTagCompound packetTag, SyncType type) {
		NBTTagList packetList = packetTag.getTagList("infoList", NBT.TAG_COMPOUND);
		boolean save = type.isType(SyncType.SAVE);
		for (int i = 0; i < packetList.tagCount(); i++) {
			NBTTagCompound infoTag = packetList.getCompoundTagAt(i);
			InfoUUID id = NBTHelper.instanceNBTSyncable(InfoUUID.class, infoTag);
			if (save) {
				PL2.getClientManager().setInfo(id, InfoHelper.readInfoFromNBT(infoTag));
			} else {
				IInfo currentInfo = PL2.getClientManager().getInfoFromUUID(id);
				if (currentInfo != null) {
					currentInfo.readData(infoTag, type);
					PL2.getClientManager().setInfo(id, currentInfo);
				}
			}
		}
	}

	public static void sendDataEmittersToPlayer(EntityPlayer player) {
		PL2.network.sendTo(new PacketClientEmitters(PL2.getWirelessManager().getClientDataEmitters(player)), (EntityPlayerMP) player);
	}

	public static void sendNormalProviderInfo(IInfoProvider monitor) {
		sendReaderToListeners(monitor, null, new InfoUUID(monitor.getIdentity(), 0)); // FIXME
	}

	public static void sendReaderFullInfo(List<PlayerListener> listeners, ILogicListenable monitor, AbstractChangeableList b, InfoUUID uuid) {
		NBTTagCompound saveTag = b != null ? InfoHelper.writeMonitoredList(new NBTTagCompound(), b, SyncType.SAVE) : null;
		if (saveTag.hasNoTags())
			return;
		listeners.forEach(listener -> PL2.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), uuid, monitor.getNetworkID(), saveTag, SyncType.SAVE), listener.player));
	}

	public static void createRapidItemUpdate(List<ItemStack> toUpdate, int networkID) {
		ItemNetworkChannels channels = NetworkHelper.getNetwork(networkID).getNetworkChannels(ItemNetworkChannels.class);
		if (channels != null) {
			channels.createRapidItemUpdate(toUpdate);
		}
	}

	public static void sendRapidItemUpdate(ILogicListenable reader, InfoUUID listUUID, ItemChangeableList list, List<ItemStack> toUpdate) {
		for (ItemStack update : toUpdate) {
			IMonitoredValue<MonitoredItemStack> value = list.find(update);
			if (value != null)
				value.setNew();
		}
		sendStandardListenerPacket(reader, list, listUUID);
	}

	public static boolean sendStandardListenerPacket(ILogicListenable reader, AbstractChangeableList updateList, InfoUUID listUUID) {
		NBTTagCompound tag = InfoHelper.writeMonitoredList(new NBTTagCompound(), updateList, SyncType.DEFAULT_SYNC);
		if (!tag.hasNoTags()) {
			List<PlayerListener> listeners = reader.getListenerList().getAllListeners(ListenerType.LISTENER); // gets display viewers also
			listeners.forEach(listener -> PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), listUUID, reader.getNetworkID(), tag, SyncType.DEFAULT_SYNC), listener.player));
			return true;
		}
		return false;
	}

	public static void sendReaderToListeners(ILogicListenable reader, AbstractChangeableList updateList, InfoUUID listUUID) {
		if (updateList == null) {
			return;
		}
		ListenerList<PlayerListener> list = reader.getListenerList();
		types: for (ListenerType type : ListenerType.ALL) {
			if (type == ListenerType.LISTENER) {
				sendStandardListenerPacket(reader, updateList, listUUID);
				continue;
			}
			List<ListenerTally<PlayerListener>> tallies = list.getTallies(type);
			if (tallies.isEmpty()) {
				continue types;
			}
			// TODO why isn't Fluid Reader connecting?
			switch (type) {
			case NEW_LISTENER:
				NBTTagCompound saveTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), updateList, SyncType.SAVE);
				if (saveTag == null || saveTag.hasNoTags())
					continue types;
				tallies.forEach(tally -> {
					PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), listUUID, reader.getNetworkID(), saveTag, SyncType.SAVE), tally.listener.player);
					tally.removeTallies(1, ListenerType.NEW_LISTENER);
					tally.addTallies(1, ListenerType.LISTENER);
					tally.source.updateState();
				});
				list.updateState();
				continue types;

			case LISTENER:
				continue;
			case TEMP_LISTENER:
				saveTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), updateList, SyncType.SAVE);
				NBTTagList tagList = new NBTTagList();
				if (reader instanceof INetworkReader) {
					INetworkReader r = (INetworkReader) reader;
					for (int i = 0; i < r.getMaxInfo(); i++) {
						InfoUUID infoID = new InfoUUID(reader.getIdentity(), i);
						IInfo info = PL2.getServerManager().getInfoFromUUID(infoID);
						if (info != null) {
							NBTTagCompound nbt = InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE);
							nbt = infoID.writeData(nbt, SyncType.SAVE);
							tagList.appendTag(nbt);
						}
					}
				}
				tallies.forEach(tally -> {
					PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), listUUID, reader.getNetworkID(), saveTag, SyncType.SAVE), tally.listener.player);
					tally.removeTallies(1, ListenerType.TEMP_LISTENER);
					sendInfoUpdatePacket(tally.listener.player, tagList, SyncType.SAVE);
				});
				continue types;
			default:
				continue types;
			}
		}
	}
}
