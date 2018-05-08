package sonar.logistics.core.tiles.readers.fluids;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.StorageSize;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.SortingDirection;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.ChannelType;
import sonar.logistics.base.channels.ContainerChannelSelection;
import sonar.logistics.base.channels.GuiChannelSelection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.core.tiles.displays.info.types.LogicInfoList;
import sonar.logistics.core.tiles.displays.info.types.fluids.FluidChangeableList;
import sonar.logistics.core.tiles.displays.info.types.fluids.InfoNetworkFluid;
import sonar.logistics.core.tiles.displays.info.types.general.LogicInfo;
import sonar.logistics.core.tiles.displays.info.types.progress.InfoProgressBar;
import sonar.logistics.core.tiles.readers.base.TileAbstractListReader;
import sonar.logistics.core.tiles.readers.fluids.FluidReader.SortingType;
import sonar.logistics.core.tiles.readers.fluids.handling.FluidNetworkChannels;
import sonar.logistics.core.tiles.readers.fluids.handling.FluidNetworkHandler;
import sonar.logistics.network.sync.SyncMonitoredType;

import java.util.List;
import java.util.Map;

public class TileFluidReader extends TileAbstractListReader<InfoNetworkFluid> implements IByteBufTile {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK, ErrorMessage.NO_FLUID_SELECTED };

	public SyncMonitoredType<InfoNetworkFluid> selected = new SyncMonitoredType<>(1);
	public SyncEnum<FluidReader.Modes> setting = (SyncEnum) new SyncEnum(FluidReader.Modes.values(), 2).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT targetSlot = (INT) new SyncTagType.INT(3).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT posSlot = (INT) new SyncTagType.INT(4).addSyncType(SyncType.SPECIAL);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 5).addSyncType(SyncType.SPECIAL);
	public SyncEnum<FluidReader.SortingType> sortingType = (SyncEnum) new SyncEnum(FluidReader.SortingType.values(), 6).addSyncType(SyncType.SPECIAL);
	public FluidSorter fluid_sorter = new FluidSorter() {
		
		public SortingDirection getDirection() {
			return sortingOrder.getObject();
		}

		public SortingType getType() {
			return sortingType.getObject();
		}

	};
	public boolean sorting_changed = true;

	{
		syncList.addParts(setting, targetSlot, posSlot, sortingOrder, sortingType, selected);
	}

	@Override
	public List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers) {
		handlers.add(FluidNetworkHandler.INSTANCE);
		return handlers;
	}

	//// ILogicReader \\\\

	@Override
	public int getMaxInfo() {
		return 1;
	}

	@Override
	public AbstractChangeableList<InfoNetworkFluid> getViewableList(AbstractChangeableList<InfoNetworkFluid> updateList, InfoUUID uuid, Map<NodeConnection, AbstractChangeableList<InfoNetworkFluid>> channels, List<NodeConnection> usedChannels) {
		if (updateList instanceof FluidChangeableList) {
			channels.values().forEach(list -> {
				if (list instanceof FluidChangeableList) {
					((FluidChangeableList) updateList).sizing.add(((FluidChangeableList) list).sizing);
				}
			});
		}
		return super.getViewableList(updateList, uuid, channels, usedChannels);
	}

	@Override
	public AbstractChangeableList<InfoNetworkFluid> sortMonitoredList(AbstractChangeableList<InfoNetworkFluid> updateInfo, int channelID) {
		return fluid_sorter.sortSaveableList(updateInfo);
	}

	@Override
	public void setMonitoredInfo(AbstractChangeableList<InfoNetworkFluid> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		IInfo info = null;
		switch (setting.getObject()) {
		case SELECTED:
			InfoNetworkFluid stack = selected.getMonitoredInfo();
			if (stack != null && stack.isValid()) {
				stack.getStoredStack().setStackSize(0);
				InfoNetworkFluid dummyInfo = new InfoNetworkFluid(stack.getStoredStack().copy(), network.getNetworkID());
				IMonitoredValue<InfoNetworkFluid> value = updateInfo.find(dummyInfo);
				info = value == null ? dummyInfo : new InfoNetworkFluid(value.getSaveableInfo().getStoredStack().copy(), network.getNetworkID()); // FIXME Make it check the EnumListChange
			}
			break;
		case POS:
			// FIXME
			break;
		case STORAGE:
			StorageSize size = updateInfo instanceof FluidChangeableList ? ((FluidChangeableList) updateInfo).sizing : new StorageSize(0, 0);
			info = new InfoProgressBar(LogicInfo.buildDirectInfo("fluid.storage", RegistryType.TILE, size.getStored()), LogicInfo.buildDirectInfo("max", RegistryType.TILE, size.getMaxStored()));
			break;
		case TANKS:
			LogicInfoList list = new LogicInfoList(getIdentity(), InfoNetworkFluid.id, this.getNetworkID());
			list.listSorter = fluid_sorter;
			info = list;
			break;
		default:
			break;
		}
		ServerInfoHandler.instance().changeInfo(this, uuid, info);

		if (sorting_changed) {
			ServerInfoHandler.instance().markChanged(this, uuid);
			sorting_changed = false;
		}
	}

	//// IChannelledTile \\\\

	@Override
	public ChannelType channelType() {
		return ChannelType.UNLIMITED;
	}

	//// PACKETS \\\\

	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		if (id == sortingOrder.id || id == sortingType.id) {
			sorting_changed = true;
		}
		// when the order of the list is changed the listeners need to recieve a full update
		if (id == 5 || id == 6) {
			FluidNetworkChannels list = network.getNetworkChannels(FluidNetworkChannels.class);
			if (list != null) {
				List<PlayerListener> players = listeners.getListeners(ListenerType.OLD_GUI_LISTENER);
				players.forEach(player -> list.sendLocalRapidUpdate(this, player.player));
			}
		}
	}

	//// GUI \\\\

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerFluidReader(this, player);
		case 1:
			return new ContainerChannelSelection(this);
		default:
			return null;
		}
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiFluidReader(this, player);
		case 1:
			return new GuiChannelSelection(player, this, 0);
		default:
			return null;
		}
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public ILogicListSorter getSorter() {
		return fluid_sorter;
	}

}