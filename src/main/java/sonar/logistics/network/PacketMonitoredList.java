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
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.helpers.InfoHelper;

public class PacketMonitoredList implements IMessage {

	public InfoUUID id;
	public int identity;
	public int networkID;
	public MonitoredList list;
	public NBTTagCompound listTag;
	public SyncType type;

	public PacketMonitoredList() {}

	public PacketMonitoredList(int identity, InfoUUID id, int networkID, NBTTagCompound listTag, SyncType type) {
		super();
		this.identity = identity;
		this.id = id;
		this.networkID = networkID;
		this.listTag = listTag;
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		identity = buf.readInt();
		networkID = buf.readInt();
		id = InfoUUID.getUUID(buf);
		type = SyncType.values()[buf.readInt()];
		list = InfoHelper.readMonitoredList(ByteBufUtils.readTag(buf), PL2.getClientManager().getMonitoredList(networkID, id).copyInfo(), type);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(identity);
		buf.writeInt(networkID);
		id.writeToBuf(buf);
		buf.writeInt(type.ordinal());
		ByteBufUtils.writeTag(buf, listTag);
	}

	public static class Handler implements IMessageHandler<PacketMonitoredList, IMessage> {

		@Override
		public IMessage onMessage(PacketMonitoredList message, MessageContext ctx) {
			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(new Runnable() {
				public void run() {
					if (message.list != null) {
						ILogicListenable viewable = PL2.getClientManager().monitors.get(message.identity);
						MonitoredList list = viewable instanceof IListReader ? ((IListReader) viewable).sortMonitoredList(message.list, message.id.channelID) : message.list;
						PL2.getClientManager().monitoredLists.put(message.id, list);
						PL2.getClientManager().onMonitoredListChanged(message.id, list);
					}
				}
			});
			return null;
		}

	}

}