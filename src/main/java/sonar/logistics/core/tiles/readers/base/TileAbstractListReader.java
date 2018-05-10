package sonar.logistics.core.tiles.readers.base;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.ISyncPart;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.readers.IReader;
import sonar.logistics.api.core.tiles.readers.channels.INetworkChannels;
import sonar.logistics.base.channels.handling.ListNetworkChannels;
import sonar.logistics.base.listeners.ListenerType;

public abstract class TileAbstractListReader<T extends IInfo> extends TileAbstractReader<T> implements IReader<T>, IFlexibleGui {

	@Override
	public IInfo getMonitorInfo(int pos) {
		return PL2.proxy.getInfoManager(isClient()).getInfoMap().get(new InfoUUID(getIdentity(), pos));
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
		if (list instanceof ListNetworkChannels) {
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
			listeners.addListener(player, ListenerType.OLD_GUI_LISTENER);
			break;
		}
	}
}
