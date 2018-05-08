package sonar.logistics.core.tiles.displays.info;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.readers.ClientLocalProvider;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.api.core.tiles.readers.INetworkReader;
import sonar.logistics.api.core.tiles.wireless.IWirelessManager;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.base.listeners.PL2ListenerList;
import sonar.logistics.core.tiles.connections.data.network.NetworkHelper;
import sonar.logistics.core.tiles.displays.DisplayHelper;
import sonar.logistics.core.tiles.displays.info.types.fluids.FluidChangeableList;
import sonar.logistics.core.tiles.displays.info.types.fluids.InfoNetworkFluid;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;
import sonar.logistics.core.tiles.misc.signaller.TileRedstoneSignaller;
import sonar.logistics.core.tiles.readers.fluids.handling.FluidNetworkChannels;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;
import sonar.logistics.core.tiles.readers.items.handling.ItemNetworkChannels;
import sonar.logistics.core.tiles.wireless.base.PacketClientEmitters;
import sonar.logistics.network.packets.PacketInfoUpdates;
import sonar.logistics.network.packets.PacketLocalProviders;
import sonar.logistics.network.packets.PacketMonitoredList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InfoPacketHelper {

	//// LOCAL PROVIDERS \\\\

	public static void sendLocalProvidersFromScreen(TileAbstractDisplay part, IBlockAccess world, BlockPos pos, EntityPlayer player) {
		List<ILogicListenable> providers = DisplayHelper.getLocalProviders(part, world, pos);
		int identity = part.getInfoContainerID();
		List<ClientLocalProvider> clientMonitors = new ArrayList<>();		
		providers.forEach(provider -> {
			provider.getListenerList().addListener(player, ListenerType.TEMPORARY_LISTENER);
			clientMonitors.add(new ClientLocalProvider(provider, provider.getSorter(), provider.getDisplayStack()));
		});
		PL2.network.sendTo(new PacketLocalProviders(clientMonitors, identity), (EntityPlayerMP) player);
	}

	public static void sendLocalProviders(TileRedstoneSignaller tileRedstoneSignaller, int identity, EntityPlayer player) {
		List<IInfoProvider> providers = tileRedstoneSignaller.getNetwork().getGlobalInfoProviders();
		List<ClientLocalProvider> clientProviders = new ArrayList<>();
		providers.forEach(provider -> {
			provider.getListenerList().addListener(player, ListenerType.TEMPORARY_LISTENER);
			clientProviders.add(new ClientLocalProvider(provider, provider.getSorter(), provider.getDisplayStack()));
		});
		PL2.network.sendTo(new PacketLocalProviders(clientProviders, identity), (EntityPlayerMP) player);
	}

	//// INFO UPDATES \\\\

	public static void createInfoUpdatesForListeners(Map<EntityPlayerMP, NBTTagList> listenerPackets, List<PlayerListener> players, NBTTagCompound updateTag, NBTTagCompound saveTag, boolean fullPacket) {
		players.forEach(player -> addPlayerUpdatesToList(listenerPackets, player.player, updateTag, saveTag, fullPacket));
	}

	public static void createInfoUpdatesForPlayers(Map<EntityPlayerMP, NBTTagList> listenerPackets, List<EntityPlayerMP> players, NBTTagCompound updateTag, NBTTagCompound saveTag, boolean fullPacket) {
		players.forEach(player -> addPlayerUpdatesToList(listenerPackets, player, updateTag, saveTag, fullPacket));
	}

	public static void addPlayerUpdatesToList(Map<EntityPlayerMP, NBTTagList> listenerPackets, EntityPlayerMP player, NBTTagCompound updateTag, NBTTagCompound saveTag, boolean fullPacket) {
		NBTTagList list = listenerPackets.get(player);
		if (list == null) {
			listenerPackets.put(player, new NBTTagList());
			list = listenerPackets.get(player);
		}
		list.appendTag(fullPacket ? saveTag.copy() : updateTag.copy());
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
				IInfo currentInfo = ClientInfoHandler.instance().getInfoFromUUID(id);
				IInfo newInfo = InfoHelper.readInfoFromNBT(infoTag);
				if (currentInfo == null || !currentInfo.isMatchingType(newInfo)) {
					ClientInfoHandler.instance().setInfo(id, newInfo);
				}else{
					currentInfo.readData(infoTag, type);
					ClientInfoHandler.instance().onInfoChanged(id, currentInfo);
				}
			} else {
				IInfo currentInfo = ClientInfoHandler.instance().getInfoFromUUID(id);
				if (currentInfo != null) {
					currentInfo.readData(infoTag, type);
					ClientInfoHandler.instance().setInfo(id, currentInfo);
				}
			}
		}
	}

	public static void sendEmittersToPlayer(EntityPlayer player, IWirelessManager manager) {
		PL2.network.sendTo(new PacketClientEmitters(manager.type(), manager.getClientEmitters(player)), (EntityPlayerMP) player);
	}

	public static void sendNormalProviderInfo(IInfoProvider monitor) {
		sendReaderToListeners(monitor, null, new InfoUUID(monitor.getIdentity(), 0));
	}

	public static void sendReaderFullInfo(List<PlayerListener> listeners, ILogicListenable monitor, AbstractChangeableList b, InfoUUID uuid) {
		NBTTagCompound saveTag = b != null ? InfoHelper.writeMonitoredList(new NBTTagCompound(), b, SyncType.SAVE) : null;
		if (saveTag.hasNoTags())
			return;
		listeners.forEach(listener -> PL2.network.sendTo(new PacketMonitoredList(monitor.getIdentity(), uuid, monitor.getNetworkID(), saveTag, SyncType.SAVE, monitor.getSorter()), listener.player));
	}

	//// RAPID UPDATES \\\\

	public static void createRapidFluidUpdate(List<FluidStack> toUpdate, int networkID) {
		FluidNetworkChannels channels = NetworkHelper.getNetwork(networkID).getNetworkChannels(FluidNetworkChannels.class);
		if (channels != null) {
			channels.createRapidFluidUpdate(toUpdate);
		}
	}

	public static void sendRapidFluidUpdate(ILogicListenable reader, InfoUUID listUUID, FluidChangeableList list, List<FluidStack> toUpdate) {
		for (FluidStack update : toUpdate) {
			IMonitoredValue<InfoNetworkFluid> value = list.find(update);
			if (value != null)
				value.setNew();
		}
		sendStandardListenerPacket(reader, list, listUUID);
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

	//// MONITORED LIST LISTENERS \\\\

	public static boolean sendStandardListenerPacket(ILogicListenable reader, AbstractChangeableList updateList, InfoUUID listUUID) {
		NBTTagCompound tag = InfoHelper.writeMonitoredList(new NBTTagCompound(), updateList, SyncType.DEFAULT_SYNC);
		if (!tag.hasNoTags()) {
			List<PlayerListener> listeners = reader.getListenerList().getAllListeners(ListenerType.OLD_GUI_LISTENER, ListenerType.OLD_DISPLAY_LISTENER); // gets display listeners also
			listeners.forEach(listener -> PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), listUUID, reader.getNetworkID(), tag, SyncType.DEFAULT_SYNC, reader.getSorter()), listener.player));
			return true;
		}
		return false;
	}

	public static void sendReaderToListeners(ILogicListenable reader, AbstractChangeableList updateList, InfoUUID listUUID) {
		if (updateList == null) {
			return;
		}
		PL2ListenerList list = reader.getListenerList();
		types: for (ListenerType type : ListenerType.ALL) {
			if (type == ListenerType.OLD_GUI_LISTENER) {// || type == ListenerType.OLD_DISPLAY_LISTENER) {
				sendStandardListenerPacket(reader, updateList, listUUID);
				continue;
			}
			List<ListenerTally<PlayerListener>> tallies = list.getTallies(type);
			if (tallies.isEmpty()) {
				continue types;
			}
			switch (type) {
			case NEW_DISPLAY_LISTENER:
			case NEW_GUI_LISTENER:
				NBTTagCompound saveTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), updateList, SyncType.SAVE);
				if (saveTag == null || saveTag.hasNoTags())
					continue types;
				tallies.forEach(tally -> {
					PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), listUUID, reader.getNetworkID(), saveTag, SyncType.SAVE, reader.getSorter()), tally.listener.player);
					tally.removeTallies(1, ListenerType.NEW_GUI_LISTENER);
					tally.addTallies(1, ListenerType.OLD_GUI_LISTENER);
					tally.source.updateState();
				});
				list.updateState();
				continue types;
			case TEMPORARY_LISTENER:
				saveTag = InfoHelper.writeMonitoredList(new NBTTagCompound(), updateList, SyncType.SAVE);
				NBTTagList tagList = new NBTTagList();
				if (reader instanceof INetworkReader) {
					INetworkReader r = (INetworkReader) reader;
					for (int i = 0; i < r.getMaxInfo(); i++) {
						InfoUUID infoID = new InfoUUID(reader.getIdentity(), i);
						IInfo info = ServerInfoHandler.instance().getInfoFromUUID(infoID);
						if (info != null) {
							NBTTagCompound nbt = InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE);
							nbt = infoID.writeData(nbt, SyncType.SAVE);
							tagList.appendTag(nbt);
						}
					}
				}
				tallies.forEach(tally -> {
					PL2.network.sendTo(new PacketMonitoredList(reader.getIdentity(), listUUID, reader.getNetworkID(), saveTag, SyncType.SAVE, reader.getSorter()), tally.listener.player);
					tally.removeTallies(1, ListenerType.TEMPORARY_LISTENER);
					sendInfoUpdatePacket(tally.listener.player, tagList, SyncType.SAVE);
				});
				continue types;
			default:
				continue types;
			}
		}
	}
}
