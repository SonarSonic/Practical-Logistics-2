package sonar.logistics.common.multiparts;

import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.multipart.ISlottedPart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.SonarMultipartInventory;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.wireless.ITransceiver;
import sonar.logistics.client.gui.GuiArray;
import sonar.logistics.common.containers.ContainerArray;
import sonar.logistics.common.multiparts.generic.SidedMultipart;
import sonar.logistics.connections.CacheHandler;
import sonar.logistics.helpers.LogisticsHelper;

public class ArrayPart extends SidedMultipart implements ISlottedPart, INode, IFlexibleGui {

	public List<NodeConnection> channels = Lists.newArrayList();
	public static boolean entityChanged = true;

	public SyncTagType.INT priority = new SyncTagType.INT(1);
	public SonarMultipartInventory inventory = new SonarMultipartInventory(this, 8) {
		@Override
		public void markDirty() {
			super.markDirty();
			updateConnectionLists();
		}

		public boolean isItemValidForSlot(int slot, ItemStack stack) {
			return stack != null && stack.getItem() instanceof ITransceiver;
		}
	};

	{
		syncList.addParts(priority, inventory);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
		if (isServer()) {
			openFlexibleGui(player, 0);
		}
		return true;
	}

	public void update() {
		super.update();
		if (isServer() && entityChanged) {
			this.updateConnectionLists();
		}
	}

	public void updateConnectionLists() {
		List<NodeConnection> channels = Lists.newArrayList();
		for (int i = 0; i < 8; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof ITransceiver && stack.hasTagCompound()) {
				NodeConnection connect = LogisticsHelper.getTransceiverNode(this, stack);
				if (!channels.contains(connect)) {
					channels.add(connect);
				}
			}
		}
		this.channels = channels;
		network.onCacheChanged(CacheHandler.NODES);
	}

	@Override
	public List<ItemStack> getDrops() {
		List<ItemStack> stacks = Lists.newArrayList();
		stacks.add(getItemStack());
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack itemstack = inventory.getStackInSlot(i);
			if (itemstack != null) {
				stacks.add(itemstack);
			}
		}
		return stacks;
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
	public void validate() {
		super.validate();
		this.updateConnectionLists();
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
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.ARRAY;
	}
}
