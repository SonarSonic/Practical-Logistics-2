package sonar.logistics.core.tiles.nodes.node;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockCoords;
import sonar.core.handlers.inventories.containers.ContainerMultipartSync;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.nodes.INode;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.events.LogisticsEventHandler;
import sonar.logistics.base.events.NetworkChanges;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.core.tiles.base.TileSidedLogistics;

import java.util.List;

public class TileNode extends TileSidedLogistics implements INode, IByteBufTile, IFlexibleGui {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK};

	public SyncTagType.INT priority = new SyncTagType.INT(1);
	{
		syncList.addPart(priority);
	}
	
	//// IConnectionNode \\\\

	@Override
	public void addConnections(List<NodeConnection> connections) {
		BlockCoords tileCoords = new BlockCoords(getPos().offset(getCableFace()), getWorld().provider.getDimension());
		connections.add(new BlockConnection(this, tileCoords, getCableFace()));
	}

	@Override
	public int getPriority() {
		return priority.getObject();
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case 1:
			priority.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case 1:
			priority.readFromBuf(buf);
			LogisticsEventHandler.instance().queueNetworkChange(network, NetworkChanges.LOCAL_CHANNELS);
			break;
		}
	}

	//// GUI \\\\

	public boolean hasStandardGui() {
		return true;
	}
	
	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(this) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiNode(this) : null;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return EnumCableRenderSize.INTERNAL;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.NODE;
	}

}
