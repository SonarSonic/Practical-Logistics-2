package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

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
import sonar.core.api.IFlexibleGui;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.filters.IFilteredTile;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.nodes.TransferType;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.readers.IdentifiedCoordsList;
import sonar.logistics.api.utils.LogisticsHelper;
import sonar.logistics.api.viewers.IViewersList;
import sonar.logistics.api.viewers.ViewerTally;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.viewers.ViewersList;
import sonar.logistics.client.gui.GuiChannelSelection;
import sonar.logistics.client.gui.GuiFilterList;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerFilterList;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.network.SyncFilterList;

public class TransferNodePart extends SidedMultipart implements IConnectionNode, IOperatorTile, IFilteredTile, IFlexibleGui, IInventoryFilter, IChannelledTile, IByteBufTile {

	public ViewersList viewers = new ViewersList(this, Lists.newArrayList(ViewerType.FULL_INFO, ViewerType.INFO));
	public static final PropertyEnum<NodeTransferMode> TRANSFER = PropertyEnum.<NodeTransferMode>create("transfer", NodeTransferMode.class);
	public SyncTagType.INT priority = new SyncTagType.INT(1);
	public SyncEnum<NodeTransferMode> transferMode = new SyncEnum(NodeTransferMode.values(), 2).setDefault(NodeTransferMode.ADD);
	public SyncFilterList filters = new SyncFilterList(3);
	public IdentifiedCoordsList list = new IdentifiedCoordsList(4);
	public SyncTagType.BOOLEAN connection = new SyncTagType.BOOLEAN(5);
	public SyncTagType.BOOLEAN items = (BOOLEAN) new SyncTagType.BOOLEAN(6).setDefault(true);
	public SyncTagType.BOOLEAN fluids = (BOOLEAN) new SyncTagType.BOOLEAN(7).setDefault(true);
	public SyncTagType.BOOLEAN energy = (BOOLEAN) new SyncTagType.BOOLEAN(8).setDefault(true);
	// public SyncTagType.BOOLEAN gases = (BOOLEAN) new SyncTagType.BOOLEAN(8).setDefault(true);
	public BlockCoords lastSelected = null;

	public int ticks = 20;
	{
		syncList.addParts(priority, transferMode, filters, list, connection, items, fluids, energy);
	}

	public TransferNodePart() {
		super(0.0625 * 8, 0, 0.0625 * 2);
	}

	public TransferNodePart(EnumFacing face) {
		super(face, 0.0625 * 8, 0, 0.0625 * 2);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
		if (!LogisticsHelper.isPlayerUsingOperator(player)) {
			if (!getWorld().isRemote) {
				openFlexibleGui(player, 0);
			}
			return true;
		}
		return false;
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

	//// IConnectionNode \\\\

	@Override
	public void addConnections(ArrayList<BlockConnection> connections) {
		// if (canConnectToNodeConnection()) {
		connections.add(getConnected());
		// }
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
		return this.wasRemoved ? null : new BlockConnection(this, new BlockCoords(getPos().offset(getFacing()), getWorld().provider.getDimension()), getFacing());
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
	public IdentifiedCoordsList getChannels() {
		return list;
	}

	@Override
	public void modifyCoords(MonitoredBlockCoords coords, int channelID) {
		lastSelected = coords.syncCoords.getCoords();
		sendByteBufPacket(-3);
	}

	//// ILogicViewable \\\\

	@Override
	public IViewersList getViewersList() {
		return viewers;
	}

	@Override
	public void onViewerAdded(EntityPlayer player, List<ViewerTally> arrayList) {
	}

	@Override
	public void onViewerRemoved(EntityPlayer player, List<ViewerTally> arrayList) {
	}

	@Override
	public UUID getIdentity() {
		return getUUID();
	}

	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(ORIENTATION, getFacing()).withProperty(TRANSFER, transferMode.getObject());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { ORIENTATION, TRANSFER });
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case -3:
			BlockCoords.writeToBuf(buf, lastSelected);
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
		case -3:
			BlockCoords coords = BlockCoords.readFromBuf(buf);
			list.modifyCoords(ChannelType.UNLIMITED, coords);
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
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.partTransferNode);
	}

}