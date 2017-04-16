package sonar.logistics.common.multiparts.readers;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.Pair;
import sonar.core.utils.SortingDirection;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.FluidReader;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.client.gui.GuiFluidReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerFluidReader;
import sonar.logistics.connections.channels.FluidNetworkChannels;
import sonar.logistics.connections.handlers.FluidNetworkHandler;
import sonar.logistics.helpers.FluidHelper;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.ProgressInfo;
import sonar.logistics.network.sync.SyncMonitoredType;

public class FluidReaderPart extends AbstractListReaderPart<MonitoredFluidStack> implements IByteBufTile {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK, TileMessage.NO_FLUID_SELECTED };

	public SyncMonitoredType<MonitoredFluidStack> selected = new SyncMonitoredType<MonitoredFluidStack>(1);
	public SyncEnum<FluidReader.Modes> setting = (SyncEnum) new SyncEnum(FluidReader.Modes.values(), 2).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT targetSlot = (INT) new SyncTagType.INT(3).addSyncType(SyncType.SPECIAL);
	public SyncTagType.INT posSlot = (INT) new SyncTagType.INT(4).addSyncType(SyncType.SPECIAL);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 5).addSyncType(SyncType.SPECIAL);
	public SyncEnum<FluidReader.SortingType> sortingType = (SyncEnum) new SyncEnum(FluidReader.SortingType.values(), 6).addSyncType(SyncType.SPECIAL);
	
	{
		syncList.addParts(setting, targetSlot, posSlot, sortingOrder, sortingType, selected);
	}

	@Override
	public List<INetworkListHandler> addValidHandlers(List<INetworkListHandler> handlers) {
		handlers.add(FluidNetworkHandler.INSTANCE);
		return handlers;
	}

	//// ILogicReader \\\\
	@Override
	public MonitoredList<MonitoredFluidStack> sortMonitoredList(MonitoredList<MonitoredFluidStack> updateInfo, int channelID) {
		FluidHelper.sortFluidList(updateInfo, sortingOrder.getObject(), sortingType.getObject());
		return updateInfo;
	}

	@Override
	public void setMonitoredInfo(MonitoredList<MonitoredFluidStack> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		IInfo info = null;
		switch (setting.getObject()) {
		case SELECTED:
			MonitoredFluidStack stack = selected.getMonitoredInfo();
			if (stack != null && stack.isValid()) {
				stack.getStoredStack().setStackSize(0);
				MonitoredFluidStack dummyInfo = stack.copy();
				Pair<Boolean, IInfo> latestInfo = updateInfo.getLatestInfo(dummyInfo);
				info = latestInfo.a ? latestInfo.b : dummyInfo;
			}
			break;
		case POS:
			break;
		case STORAGE:
			info = new ProgressInfo(LogicInfo.buildDirectInfo("fluid.storage", RegistryType.TILE, updateInfo.sizing.getStored()), LogicInfo.buildDirectInfo("max", RegistryType.TILE, updateInfo.sizing.getMaxStored()));
			break;
		case TANKS:
			info = new LogicInfoList(getIdentity(), MonitoredFluidStack.id, this.getNetworkID());
			break;
		default:
			break;
		}
		PL2.getServerManager().changeInfo(uuid, info);
	}

	//// IChannelledTile \\\\

	@Override
	public ChannelType channelType() {
		return ChannelType.UNLIMITED;
	}

	//// PACKETS \\\\

	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		// when the order of the list is changed the viewers need to recieve a full update
		if (id == 5 || id == 6) {
			FluidNetworkChannels list = network.getNetworkChannels(FluidNetworkChannels.class);
			if (list != null) {
				List<PlayerListener> players = listeners.getListeners(ListenerType.INFO);
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
	public TileMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.FLUID_READER;
	}

}