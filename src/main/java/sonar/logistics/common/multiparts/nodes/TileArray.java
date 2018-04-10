package sonar.logistics.common.multiparts.nodes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.SonarInventory;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.wireless.ITransceiver;
import sonar.logistics.client.gui.GuiArray;
import sonar.logistics.common.containers.ContainerArray;
import sonar.logistics.common.multiparts.TileSidedLogistics;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.networking.CacheHandler;

public class TileArray extends TileSidedLogistics implements INode, IFlexibleGui {

	public static boolean entityChanged = true;
	public List<NodeConnection> channels = new ArrayList<>();

	public SyncTagType.INT priority = new SyncTagType.INT(1);
	public SonarInventory inventory = new SonarInventory(this, 8) {
		@Override
		public void markDirty() {
			super.markDirty();
			updateConnectionLists();
		}

		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			return stack != null && stack.getItem() instanceof ITransceiver;
		}

		public SyncType[] getSyncTypes() {
			return new SyncType[] { SyncType.SAVE, SyncType.DEFAULT_SYNC };
		}
	};

	{
		syncList.addParts(priority, inventory);
	}

	public void update() {
		super.update();
		/* TODO update entities properly if (isServer() && entityChanged) { this.updateConnectionLists(); } */
	}

	public void updateConnectionLists() {
		List<NodeConnection> channels = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof ITransceiver && stack.hasTagCompound()) {
				NodeConnection connect = LogisticsHelper.getTransceiverNode(this, world, stack);
				if (!channels.contains(connect)) {
					channels.add(connect);
				}
			}
		}
		this.channels = channels;
		network.onCacheChanged(CacheHandler.NODES);
	}

	//// IConnectionNode \\\\
	@Override
	public int getPriority() {
		return priority.getObject();
	}

	@Override
	public void addConnections(List<NodeConnection> connections) {
		connections.addAll(channels);
	}

	//// EVENTS \\\\
	public void onFirstTick() {
		super.onFirstTick();
		this.updateConnectionLists();
	}

	public void onSyncPacketRequested(EntityPlayer player) {
		super.onSyncPacketRequested(player);
		inventory.markChanged();
	}

	//// GUI \\\\

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		if (id == 0)
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerArray(player, this) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiArray(player, this) : null;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.HALF;
	}
}
