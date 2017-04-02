package sonar.logistics.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.Logistics;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.readers.IListReader;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoHelper;

public class PacketMonitoredList implements IMessage {

	public UUID identity;
	public InfoUUID id;
	public int networkID;
	public MonitoredList list;
	public NBTTagCompound listTag;
	public SyncType type;

	public PacketMonitoredList() {
	}

	public PacketMonitoredList(UUID identity, InfoUUID id, int networkID, NBTTagCompound listTag, SyncType type) {
		super();
		this.identity = identity;
		this.id = id;
		this.networkID = networkID;
		this.listTag = listTag;
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		long msb = buf.readLong();
		long lsb = buf.readLong();
		identity = new UUID(msb, lsb);
		networkID = buf.readInt();
		id = InfoUUID.getUUID(buf);
		type = SyncType.values()[buf.readInt()];
		list = InfoHelper.readMonitoredList(ByteBufUtils.readTag(buf), Logistics.getClientManager().getMonitoredList(networkID, id).copyInfo(), type);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(identity.getMostSignificantBits());
		buf.writeLong(identity.getLeastSignificantBits());
		buf.writeInt(networkID);
		id.writeToBuf(buf);
		buf.writeInt(type.ordinal());
		ByteBufUtils.writeTag(buf, listTag);
	}

	public static class Handler implements IMessageHandler<PacketMonitoredList, IMessage> {

		@Override
		public IMessage onMessage(PacketMonitoredList message, MessageContext ctx) {
			if(message.list.get(0) instanceof MonitoredItemStack){
				System.out.println("stack");
			}
			SonarCore.proxy.getThreadListener(ctx).addScheduledTask(new Runnable() {
				public void run() {
					if (message.list != null) {
						ILogicViewable viewable = Logistics.getClientManager().monitors.get(message.identity);
						if (viewable instanceof IListReader) {
							Logistics.getClientManager().monitoredLists.put(message.id, ((IListReader) viewable).sortMonitoredList(message.list, message.id.channelID));
						} else {
							Logistics.getClientManager().monitoredLists.put(message.id, message.list);
						}
					}
				}
			});
			return null;
		}

	}

}