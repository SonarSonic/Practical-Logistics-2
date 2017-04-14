package sonar.logistics.common.multiparts.nodes;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mcmultipart.MCMultiPartMod;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncUUID;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.filters.ITransferFilteredTile;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.IChannelledTile;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.api.tiles.nodes.TransferType;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.client.gui.generic.GuiFilterList;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerFilterList;
import sonar.logistics.common.multiparts.SidedPart;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredEntity;
import sonar.logistics.network.sync.SyncFilterList;

public class TransferNodePart extends SidedPart implements INode, IOperatorTile, ITransferFilteredTile, IFlexibleGui, IInventoryFilter, IChannelledTile, IByteBufTile {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK};

	public ListenableList<PlayerListener> listeners = new ListenableList(this, ListenerType.ALL.size());
	public static final PropertyEnum<NodeTransferMode> TRANSFER = PropertyEnum.<NodeTransferMode>create("transfer", NodeTransferMode.class);
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
	public boolean performOperation(AdvancedRayTraceResultPart rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!getWorld().isRemote) {
			transferMode.incrementEnum();
			sendSyncPacket();
			sendUpdatePacket(true);
			FontHelper.sendMessage("Transfer Mode: " + transferMode.getObject(), getWorld(), player);
		}
		return true;
	}

	@Override
	public boolean allowed(ItemStack stack) {
		return filters.matches(new StoredItemStack(stack), transferMode.getObject());
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
		return this.wasRemoved ? null : new BlockConnection(this, new BlockCoords(getPos().offset(getCableFace()), getWorld().provider.getDimension()), getCableFace());
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
			lastSelected.setCoords(((MonitoredBlockCoords) info).syncCoords.getCoords());
			sendByteBufPacket(-3);
		}
		if (info instanceof MonitoredEntity) {
			lastSelectedUUID.setObject(((MonitoredEntity) info).uuid.getUUID());
			sendByteBufPacket(-4);
		}
	}

	//// ILogicViewable \\\\

	public ListenableList<PlayerListener> getListenerList() {
		return listeners;
	}

	//// EVENTS \\\\

	public void validate() {
		super.validate();
		if (isClient())
			this.requestSyncPacket();
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(PL2Properties.ORIENTATION, getCableFace()).withProperty(TRANSFER, transferMode.getObject());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { PL2Properties.ORIENTATION, TRANSFER });
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
			sendSyncPacket();
			sendUpdatePacket(true);
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
	public TileMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.TRANSFER_NODE;
	}

}