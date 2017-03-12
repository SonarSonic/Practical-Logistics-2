package sonar.logistics.common.multiparts;

import java.util.List;

import io.netty.buffer.ByteBuf;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.SonarHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.EntityTarget;
import sonar.logistics.api.nodes.IEntityNode;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.utils.LogisticsHelper;
import sonar.logistics.client.gui.GuiEntityNode;
import sonar.logistics.client.gui.GuiNode;

public class EntityNodePart extends SidedMultipart implements IEntityNode, IByteBufTile, IFlexibleGui {

	public SyncEnum<EntityTarget> entityTarget = new SyncEnum<EntityTarget>(EntityTarget.values(), 1);
	public SyncTagType.INT entityRange = (INT) new SyncTagType.INT(2).setDefault(10);
	public SyncTagType.BOOLEAN nearest = (BOOLEAN) new SyncTagType.BOOLEAN(3).setDefault(true);
	{
		syncList.addParts(entityTarget, entityRange, nearest);
	}

	public EntityNodePart() {
		super(5 * 0.0625, 0.0625 * 1, 0.0625 * 4);
	}

	public EntityNodePart(EnumFacing face) {
		super(face, 5 * 0.0625, 0.0625 * 1, 0.0625 * 4);
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

	public Entity getEntity() {
		switch (entityTarget.getObject()) {
		case ANIMAL:
			return SonarHelper.getEntity(EntityAnimal.class, this, entityRange.getObject(), nearest.getObject());
		case MOB:
			return SonarHelper.getEntity(EntityMob.class, this, entityRange.getObject(), nearest.getObject());
		case NORMAL:
			return SonarHelper.getEntity(Entity.class, this, entityRange.getObject(), nearest.getObject());
		case PLAYER:
			return SonarHelper.getEntity(EntityPlayer.class, this, entityRange.getObject(), nearest.getObject());
		default:
			return null;
		}
	}

	@Override
	public void addEntities(List<NodeConnection> entities) {
		Entity entity = getEntity();
		if (entity != null)
			entities.add(new EntityConnection(this, entity));
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			entityTarget.writeToBuf(buf);
			break;
		case 3:
			nearest.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			entityTarget.readFromBuf(buf);
			break;
		case 1:
			if (entityRange.getObject() != 64)
				entityRange.increaseBy(1);
			break;
		case 2:
			if (entityRange.getObject() != 1)
				entityRange.decreaseBy(1);
			break;
		case 3:
			nearest.readFromBuf(buf);
			break;
		}
	}

	//// GUI \\\\

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(this) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiEntityNode(this) : null;
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
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.partEntityNode);
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
