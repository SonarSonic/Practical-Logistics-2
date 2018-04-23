package sonar.logistics.common.multiparts.nodes;

import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
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
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.states.ErrorMessage;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.EntityTarget;
import sonar.logistics.api.tiles.nodes.INode;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.client.gui.GuiEntityNode;
import sonar.logistics.common.multiparts.TileSidedLogistics;
import sonar.logistics.networking.CacheHandler;

public class TileEntityNode extends TileSidedLogistics implements INode, IByteBufTile, IFlexibleGui {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK };

	public SyncEnum<EntityTarget> entityTarget = new SyncEnum<EntityTarget>(EntityTarget.values(), 1);
	public SyncTagType.INT entityRange = (INT) new SyncTagType.INT(2).setDefault(10);
	public SyncTagType.BOOLEAN nearest = (BOOLEAN) new SyncTagType.BOOLEAN(3).setDefault(true);
	public UUID uuid;

	public int update;
	{
		syncList.addParts(entityTarget, entityRange, nearest);
	}

	public void update() {
		super.update();
		if(world.isRemote){
			return;
		}
		if (update == 20) {
			Entity entity = getEntity();
			if ((uuid == null && entity != null || entity == null || !uuid.equals(entity.getPersistentID()))) {
				network.onCacheChanged(CacheHandler.ENTITY_NODES); // TODO version for Arrays
			}
			update = 0;
		} else {
			update++;
		}
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
	public void addConnections(List<NodeConnection> connections) {
		Entity entity = getEntity();
		if (entity != null) {
			uuid = entity.getPersistentID();
			connections.add(new EntityConnection(this, entity));
		} else {
			uuid = null;
		}
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

	public boolean hasStandardGui() {
		return true;
	}

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
	public int getPriority() {
		return 0;
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.ENTITY_NODE;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.NONE;
	}
}
