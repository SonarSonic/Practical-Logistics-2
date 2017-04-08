package sonar.logistics.network;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.readers.ClientViewable;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.viewers.ILogicViewable;

public class PacketViewables implements IMessage {

	public ArrayList<ClientViewable> viewables;
	public int screenIdentity;

	public PacketViewables() {
	}

	public PacketViewables(ArrayList<ClientViewable> viewables, int screenIdentity) {
		this.viewables = viewables;
		this.screenIdentity = screenIdentity;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		screenIdentity = buf.readInt();
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		viewables = new ArrayList();
		if (tag.hasKey("monitors")) {
			NBTTagList tagList = tag.getTagList("monitors", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				viewables.add(NBTHelper.instanceNBTSyncable(ClientViewable.class, tagList.getCompoundTagAt(i)));
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(screenIdentity);
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList tagList = new NBTTagList();
		viewables.forEach(emitter -> tagList.appendTag(emitter.writeData(new NBTTagCompound(), SyncType.SAVE)));
		if (!tagList.hasNoTags()) {
			tag.setTag("monitors", tagList);
		}
		ByteBufUtils.writeTag(buf, tag);

	}

	public static class Handler implements IMessageHandler<PacketViewables, IMessage> {
		@Override
		public IMessage onMessage(PacketViewables message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				Map<Integer, ArrayList<ClientViewable>> monitors = PL2.getClientManager().clientLogicMonitors;
				if (monitors.get(message.screenIdentity) == null) {
					monitors.put(message.screenIdentity, message.viewables);
				} else {
					monitors.get(message.screenIdentity).clear();
					monitors.get(message.screenIdentity).addAll(message.viewables);
				}
				ArrayList<Object> cache = new ArrayList();
				for (ClientViewable clientMonitor : message.viewables) {
					ILogicViewable monitor = clientMonitor.getViewable();					
					if (monitor != null && monitor instanceof IInfoProvider) {
						int hashCode = monitor.getIdentity();
						cache.add(monitor);
						for (int i = 0; i < ((IInfoProvider) monitor).getMaxInfo(); i++) {
							cache.add(new InfoUUID(hashCode, i));
						}
					}
				}

				Map<Integer, ArrayList<Object>> sortedMonitors = PL2.getClientManager().sortedLogicMonitors;
				if (sortedMonitors.get(message.screenIdentity) == null) {
					sortedMonitors.put(message.screenIdentity, cache);
				} else {
					sortedMonitors.get(message.screenIdentity).clear();
					sortedMonitors.get(message.screenIdentity).addAll(cache);
				}

			}
			return null;
		}
	}

}
