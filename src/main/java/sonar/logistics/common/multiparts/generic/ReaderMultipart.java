package sonar.logistics.common.multiparts.generic;

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
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.ISyncPart;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.readers.IReader;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.helpers.LogisticsHelper;

public abstract class ReaderMultipart<T extends IMonitorInfo> extends MonitorMultipart<T> implements ISlottedPart, IReader<T>, IFlexibleGui {

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
	public IMonitorInfo getMonitorInfo(int pos) {
		return PL2.getInfoManager(this.getWorld().isRemote).getInfoList().get(new InfoUUID(getIdentity(), pos));
	}

	@Override
	public NetworkConnectionType canConnect(EnumFacing dir) {
		return dir != getCableFace() ? NetworkConnectionType.NETWORK : NetworkConnectionType.VISUAL;
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.writeToBuf(buf);
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		ISyncPart part = NBTHelper.getSyncPartByID(syncList.getStandardSyncParts(), id);
		if (part != null)
			part.readFromBuf(buf);
	}

	//// GUI \\\\

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		super.onGuiOpened(obj, id, world, player, tag);
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			listeners.addListener(player, ListenerType.FULL_INFO);
			break;
		}
	}
}
