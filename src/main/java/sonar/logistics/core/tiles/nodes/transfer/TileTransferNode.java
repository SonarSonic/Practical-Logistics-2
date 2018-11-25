package sonar.logistics.core.tiles.nodes.transfer;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FontHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.api.core.items.operator.IOperatorTile;
import sonar.logistics.api.core.items.operator.OperatorMode;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.nodes.INode;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.core.tiles.nodes.TransferType;
import sonar.logistics.base.channels.*;
import sonar.logistics.base.filters.ContainerFilterList;
import sonar.logistics.base.filters.GuiFilterList;
import sonar.logistics.base.filters.ITransferFilteredTile;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.base.listeners.ListenerType;
import sonar.logistics.base.listeners.PL2ListenerList;
import sonar.logistics.base.tiles.IChannelledTile;
import sonar.logistics.core.tiles.base.TileSidedLogistics;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredEntity;
import sonar.logistics.network.sync.SyncFilterList;

import java.util.List;
import java.util.function.Predicate;

public class TileTransferNode extends TileSidedLogistics implements INode, IOperatorTile, ITransferFilteredTile, IFlexibleGui, IChannelledTile, IByteBufTile {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK };

	public PL2ListenerList listeners = new PL2ListenerList(this, ListenerType.ALL.size());
	public SyncTagType.INT priority = new SyncTagType.INT(1);
	public SyncEnum<NodeTransferMode> transferMode = new SyncEnum(NodeTransferMode.values(), 2).setDefault(NodeTransferMode.ADD);
	public SyncFilterList filters = new SyncFilterList(3);
	public ChannelList list = new ChannelList(getIdentity(), this.channelType(), 4);
	public SyncTagType.BOOLEAN connection = new SyncTagType.BOOLEAN(5);
	public SyncTagType.BOOLEAN items = (BOOLEAN) new SyncTagType.BOOLEAN(6).setDefault(true);
	public SyncTagType.BOOLEAN fluids = (BOOLEAN) new SyncTagType.BOOLEAN(7).setDefault(true);
	public SyncTagType.BOOLEAN energy = (BOOLEAN) new SyncTagType.BOOLEAN(8).setDefault(true);
	public SyncCoords lastSelected = new SyncCoords(-11);
	public SyncUUID lastSelectedUUID = new SyncUUID(-10);
	// public SyncTagType.BOOLEAN gases = (BOOLEAN) new SyncTagType.BOOLEAN(8).setDefault(true);

	public int ticks = 20;
	{
		syncList.addParts(priority, transferMode, filters, list, connection, items, fluids, energy);
	}

	@Override
	public boolean performOperation(RayTraceResult rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!getWorld().isRemote) {
			transferMode.incrementEnum();
			SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
			//sendSyncPacket();
			//sendUpdatePacket(true);
			FontHelper.sendMessage("Transfer Mode: " + transferMode.getObject(), getWorld(), player);
		}
		return true;
	}

	@Override
	public Predicate<ItemStack> getFilter() {
		return s -> filters.matches(s, transferMode.getObject());
	}

	@Override
	public boolean allowed(FluidStack stack) {
		return filters.matches(new StoredFluidStack(stack), transferMode.getObject());
	}

	//// IConnectionNode \\\\
	@Override
	public void addConnections(List<NodeConnection> connections) {
		connections.add(getConnected());
	}

	@Override
	public int getPriority() {
		return priority.getObject();
	}

	//// IFilteredTile \\\\
	@Override
	public SyncFilterList getFilters() {
		return filters;
	}

	@Override
	public NodeTransferMode getTransferMode() {
		return transferMode.getObject();
	}

	@Override
	public boolean isTransferEnabled(TransferType type) {
		switch (type) {
		case ENERGY:
			return energy.getObject();
		case FLUID:
			return fluids.getObject();
		case GAS:
			// return gases.getObject();
		case INFO:
			break;
		case ITEMS:
			return items.getObject();
		default:
			break;

		}
		return false;
	}

	@Override
	public void setTransferType(TransferType type, boolean enable) {
		switch (type) {
		case ENERGY:
			energy.setObject(enable);
			break;
		case FLUID:
			fluids.setObject(enable);
			break;
		case GAS:
			// gases.setObject(enable);
		case INFO:
			break;
		case ITEMS:
			items.setObject(enable);
			break;
		default:
			break;
		}
		this.sendByteBufPacket(2);
	}

	@Override
	public BlockConnection getConnected() {
		return new BlockConnection(this, new BlockCoords(getPos().offset(getCableFace()), getWorld().provider.getDimension()), getCableFace());
	}

	@Override
	public boolean canConnectToNodeConnection() {
		return connection.getObject();
	}

	@Override
	public void incrementTransferMode() {
		transferMode.incrementEnum();
		sendByteBufPacket(3);
	}

	//// IOperatorProvider \\\\

	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Transfer Mode: " + transferMode.getObject());
	}

	//// IChannelledTile \\\\

	@Override
	public ChannelType channelType() {
		return ChannelType.UNLIMITED;
	}

	@Override
	public ChannelList getChannels() {
		return list;
	}

	@Override
	public void sendCoordsToServer(IInfo info, int channelID) {
		if (info instanceof MonitoredBlockCoords) {
			lastSelected.setCoords(((MonitoredBlockCoords) info).getCoords());
			sendByteBufPacket(-3);
		}
		if (info instanceof MonitoredEntity) {
			lastSelectedUUID.setObject(((MonitoredEntity) info).getUUID());
			sendByteBufPacket(-4);
		}
	}

	//// ILogicViewable \\\\

	public PL2ListenerList getListenerList() {
		return listeners;
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case -4:
			lastSelectedUUID.writeToBuf(buf);
			break;
		case -3:
			lastSelected.writeToBuf(buf);
			break;
		case 1:
			list.writeToBuf(buf);
			break;
		case 2:
			items.writeToBuf(buf);
			fluids.writeToBuf(buf);
			energy.writeToBuf(buf);
			// gases.writeToBuf(buf);
			break;
		case 3:
			transferMode.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case -4:
			lastSelectedUUID.readFromBuf(buf);
			list.give(lastSelectedUUID.getUUID());
			sendByteBufPacket(1);
			break;
		case -3:
			lastSelected.readFromBuf(buf);
			list.give(lastSelected.getCoords());
			sendByteBufPacket(1);
			break;
		case 1:
			list.readFromBuf(buf);
			break;
		case 2:
			items.readFromBuf(buf);
			fluids.readFromBuf(buf);
			energy.readFromBuf(buf);
			// gases.readFromBuf(buf);
			break;
		case 3:
			transferMode.readFromBuf(buf);
			//sendSyncPacket();
			SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
			//sendUpdatePacket(true);
			break;
		}
	}

	//// GUI \\\\

	public boolean hasStandardGui() {
		return true;
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerFilterList(player, this);
		case 1:
			return new ContainerChannelSelection(this);
		}
		return null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiFilterList(player, this, id);
		case 1:
			return new GuiChannelSelection(player, this, 0);
		}
		return null;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		case 1:
			sendNetworkCoordMap(player);
			list.markDirty();
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return EnumCableRenderSize.INTERNAL;
	}

}