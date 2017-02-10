package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.SonarAPI;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.InventoryHelper.IInventoryFilter;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.Pair;
import sonar.logistics.Logistics;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.cabling.IChannelledTile;
import sonar.logistics.api.connecting.RefreshType;
import sonar.logistics.api.filters.IFilteredTile;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.readers.ILogicMonitor;
import sonar.logistics.api.readers.IdentifiedCoordsList;
import sonar.logistics.api.viewers.IViewersList;
import sonar.logistics.api.viewers.ViewerTally;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.api.viewers.ViewersList;
import sonar.logistics.client.gui.GuiChannelSelection;
import sonar.logistics.client.gui.GuiFilterList;
import sonar.logistics.client.gui.GuiInfoReader;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerFilterList;
import sonar.logistics.common.containers.ContainerInfoReader;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.network.PacketMonitoredCoords;
import sonar.logistics.network.SyncFilterList;

public class TransferNodePart extends SidedMultipart implements IConnectionNode, IOperatorTile, IFilteredTile, IFlexibleGui, IInventoryFilter, IChannelledTile, IByteBufTile {

	public ViewersList viewers = new ViewersList(this, Lists.newArrayList(ViewerType.FULL_INFO, ViewerType.INFO));
	public static final PropertyEnum<NodeTransferMode> TRANSFER = PropertyEnum.<NodeTransferMode>create("transfer", NodeTransferMode.class);
	public SyncEnum<NodeTransferMode> transferMode = new SyncEnum(NodeTransferMode.values(), 1).setDefault(NodeTransferMode.ADD);
	public SyncFilterList filters = new SyncFilterList(2);
	public IdentifiedCoordsList list = new IdentifiedCoordsList(3);
	public BlockCoords lastSelected = null;

	public int ticks = 20;
	{
		syncList.addParts(transferMode, filters, list);
	}

	public TransferNodePart() {
		super(0.0625 * 8, 0, 0.0625 * 2);
	}

	public TransferNodePart(EnumFacing face) {
		super(face, 0.0625 * 8, 0, 0.0625 * 2);
	}

	public void update() {
		super.update();
		if (this.isClient()) {
			return;
		}
		if (!transferMode.getObject().isPassive()) {
			if (ticks >= 20) {
				ticks = 0;
				TileEntity localTile = new BlockCoords(getPos().offset(getFacing()), getWorld().provider.getDimension()).getTileEntity(getWorld());
				if (localTile != null) {

					ArrayList<NodeConnection> tiles = network.getExternalBlocks(true);
					for (NodeConnection entry : tiles) {
						TileEntity netTile = entry.coords.getTileEntity();
						if (netTile != null) {
							EnumFacing dirFrom = transferMode.getObject().shouldRemove() ? getFacing() : entry.face;
							EnumFacing dirTo = !transferMode.getObject().shouldRemove() ? getFacing() : entry.face;
							TileEntity from = transferMode.getObject().shouldRemove() ? localTile : netTile;
							TileEntity to = !transferMode.getObject().shouldRemove() ? localTile : netTile;
							SonarAPI.getItemHelper().transferItems(from, to, dirFrom.getOpposite(), dirTo.getOpposite(), this);
						}
					}

				}
			} else {
				ticks++;
			}
		}
	}

	@Override
	public void addConnections(ArrayList<NodeConnection> connections) {
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
	public SyncFilterList getFilters() {
		return filters;
	}

	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Transfer Mode: " + transferMode.getObject());
	}

	@Override
	public int getPriority() {
		return 0; // TODO
	}

	@Override
	public IViewersList getViewersList() {
		return viewers;
	}

	@Override
	public boolean allowed(ItemStack stack) {
		return filters.matches(new StoredItemStack(stack), transferMode.getObject());
	}

	@Override
	public IdentifiedCoordsList getChannels(int channelID) {
		return list;
	}

	@Override
	public void modifyCoords(MonitoredBlockCoords coords, int channelID) {
		lastSelected = coords.syncCoords.getCoords();
		sendByteBufPacket(-3);
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case -3:
			BlockCoords.writeToBuf(buf, lastSelected);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case -3:
			BlockCoords coords = BlockCoords.readFromBuf(buf);
			list.modifyCoords(ChannelType.UNLIMITED, coords);
			sendByteBufPacket(list.tagID);
			break;
		}
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

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.partTransferNode);
	}

	@Override
	public IBlockState getActualState(IBlockState state) {
		World w = getContainer().getWorldIn();
		BlockPos pos = getContainer().getPosIn();
		return state.withProperty(ORIENTATION, getFacing()).withProperty(TRANSFER, transferMode.getObject());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { ORIENTATION, TRANSFER });
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

}
