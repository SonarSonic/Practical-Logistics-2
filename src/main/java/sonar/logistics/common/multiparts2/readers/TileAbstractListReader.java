package sonar.logistics.common.multiparts2.readers;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.ISyncPart;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.readers.IReader;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.common.multiparts.AbstractReaderPart;
import sonar.logistics.connections.channels.ListNetworkChannels;

public abstract class TileAbstractListReader<T extends IInfo> extends TileAbstractReader<T> implements IReader<T>, IFlexibleGui {

	@Override
	public IInfo getMonitorInfo(int pos) {
		return PL2.getInfoManager(this.getWorld().isRemote).getInfoList().get(new InfoUUID(getIdentity(), pos));
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

	public void sendRapidUpdate(EntityPlayer player) {
		INetworkChannels list = getNetworkChannels();
		if (list != null && list instanceof ListNetworkChannels) {
			((ListNetworkChannels) list).sendLocalRapidUpdate(this, player);
		}
	}

	public INetworkChannels getNetworkChannels() {
		return network.getNetworkChannels(getValidHandlers().get(0).getChannelsType());
	}

	//// GUI \\\\

	public boolean hasStandardGui() {
		return true;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		super.onGuiOpened(obj, id, world, player, tag);
		switch (id) {
		case 0:
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			sendRapidUpdate(player);
			listeners.addListener(player, ListenerType.INFO);
			break;
		}
	}
}
