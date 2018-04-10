package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.lists.types.InfoChangeableList;
import sonar.logistics.networking.ClientInfoHandler;
import sonar.logistics.networking.info.InfoHelper;

public class PacketChannels implements IMessage {

	public InfoChangeableList list;
	public NBTTagCompound listTag;
	public int registryID;

	public PacketChannels() {}

	public PacketChannels(int registryID, NBTTagCompound listTag) {
		this.listTag = listTag;
		this.registryID = registryID;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		registryID = buf.readInt();
		listTag = ByteBufUtils.readTag(buf);
		if (listTag != null) {
			list = InfoHelper.readMonitoredList(listTag, ClientInfoHandler.instance().channelMap.getOrDefault(registryID, new InfoChangeableList()), SyncType.DEFAULT_SYNC);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(registryID);
		ByteBufUtils.writeTag(buf, listTag);
	}

	public static class Handler implements IMessageHandler<PacketChannels, IMessage> {
		@Override
		public IMessage onMessage(PacketChannels message, MessageContext ctx) {
			if (message.list != null)
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> ClientInfoHandler.instance().channelMap.put(message.registryID, message.list));
			return null;
		}
	}

}
