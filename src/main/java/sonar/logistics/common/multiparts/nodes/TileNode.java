package sonar.logistics.common.multiparts.nodes;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockCoords;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.client.gui.GuiNode;
import sonar.logistics.common.multiparts.TileSidedLogistics;

public class TileNode extends TileSidedLogistics implements INode, IByteBufTile, IFlexibleGui {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK};

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
			network.onConnectionChanged(this);
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
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.INTERNAL;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.NODE;
	}

}
