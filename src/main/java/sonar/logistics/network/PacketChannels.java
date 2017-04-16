package sonar.logistics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.helpers.InfoHelper;

public class PacketChannels implements IMessage {

	public MonitoredList<IInfo> list;
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
			list = InfoHelper.readMonitoredList(listTag, PL2.getClientManager().channelMap.getOrDefault(registryID, MonitoredList.newMonitoredList(registryID)).copyInfo(), SyncType.DEFAULT_SYNC);
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
			SonarCore.proxy.getThreadListener(ctx).addScheduledTask(new Runnable() {
				public void run() {
					if (message.list != null)
						PL2.getClientManager().channelMap.put(message.registryID, message.list);
				}
			});
			return null;
		}
	}

}
