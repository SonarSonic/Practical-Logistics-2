package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import mcmultipart.multipart.ISlottedPart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.SonarMultipartInventory;
import sonar.core.network.sync.SyncTagType;
import sonar.core.utils.IGuiTile;
import sonar.logistics.Logistics;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.connecting.RefreshType;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.IConnectionNode;
import sonar.logistics.api.nodes.IEntityNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.wireless.IEntityTransceiver;
import sonar.logistics.api.wireless.ITileTransceiver;
import sonar.logistics.api.wireless.ITransceiver;
import sonar.logistics.client.gui.GuiArray;
import sonar.logistics.common.containers.ContainerArray;

public class ArrayPart extends SidedMultipart implements ISlottedPart, IConnectionNode, IEntityNode, IGuiTile {

	public SyncTagType.INT priority = new SyncTagType.INT(1);
	public ArrayList<BlockConnection> coordList = Lists.newArrayList();
	public ArrayList<EntityConnection> entityList = Lists.newArrayList();
	public static boolean entityChanged = true;
	{
		syncList.addPart(priority);
	}
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

	public ArrayPart() {
		super(0.625, 0.0625 * 1, 0.0625 * 4);
		syncList.addPart(inventory);
	}

	public ArrayPart(EnumFacing face) {
		super(face, 0.625, 0.0625 * 1, 0.0625 * 4);
		syncList.addPart(inventory);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack heldItem, PartMOP hit) {
		if (!this.getWorld().isRemote) {
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			openGui(player, Logistics.instance);
		}
		return false;
	}

	public void update() {
		super.update();
		if (isServer() && entityChanged) {
			this.updateConnectionLists();
		}
	}

	public void updateConnectionLists() {
		ArrayList<BlockConnection> coordList = Lists.newArrayList();
		ArrayList<EntityConnection> entityList = Lists.newArrayList();
		for (int i = 0; i < 8; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.hasTagCompound()) {
				if (stack.getItem() instanceof ITileTransceiver) {
					ITileTransceiver trans = (ITileTransceiver) stack.getItem();
					coordList.add(new BlockConnection(this, trans.getCoords(stack), trans.getDirection(stack)));
				}
				if (stack.getItem() instanceof IEntityTransceiver) {
					IEntityTransceiver trans = (IEntityTransceiver) stack.getItem();
					UUID uuid = trans.getEntityUUID(stack);
					if (uuid != null) {
						for (Entity entity : getWorld().getLoadedEntityList()) {
							if (entity.getPersistentID().equals(uuid)) {
								entityList.add(new EntityConnection(this, entity));
								break;
							}
						}
					}
				}
			}
		}
		this.coordList = coordList;
		this.entityList = entityList;
		network.markDirty(RefreshType.FULL);
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
	public void addConnections(ArrayList<NodeConnection> connections) {
		connections.addAll(coordList);
	}

	//// IEntityNode \\\\

	@Override
	public void addEntities(List<NodeConnection> entities) {
		entities.addAll(entityList);
	}

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.partArray);
	}

	//// EVENTS \\\\

	public void onFirstTick() {
		super.onFirstTick();
		this.updateConnectionLists();
	}

	//// GUI \\\\

	@Override
	public Object getGuiContainer(EntityPlayer player) {
		return new ContainerArray(player, this);
	}

	@Override
	public Object getGuiScreen(EntityPlayer player) {
		return new GuiArray(player, this);
	}
}
