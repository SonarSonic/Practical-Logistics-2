package sonar.logistics.common.multiparts;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockCoords;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.Pair;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.connecting.RefreshType;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.client.gui.GuiNode;
import sonar.logistics.helpers.LogisticsHelper;

public class NodePart extends SidedMultipart implements IConnectionNode, ISlottedPart, IByteBufTile, IFlexibleGui {

	public SyncTagType.INT priority = new SyncTagType.INT(1);
	{
		syncList.addPart(priority);
	}

	public NodePart() {
		super(0.875, 0, 0.0625);
	}

	public NodePart(EnumFacing face) {
		super(face, 0.875, 0, 0.0625);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
		if (!LogisticsHelper.isPlayerUsingOperator(player)) {
			if (!getWorld().isRemote) {
				SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
				openFlexibleGui(player, 0);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void addConnections(ArrayList<NodeConnection> connections) {
		BlockCoords tileCoords = new BlockCoords(getPos().offset(face), getWorld().provider.getDimension());		
		connections.add(new NodeConnection(this, tileCoords, face));
	}

	@Override
	public int getPriority() {
		return priority.getObject();
	}

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.partNode);
	}

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
			this.network.markDirty(RefreshType.CONNECTED_BLOCKS);		
			break;
		}
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(this) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiNode(this) : null;
	}
}
