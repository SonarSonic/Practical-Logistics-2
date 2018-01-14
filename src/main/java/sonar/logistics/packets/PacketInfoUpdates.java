package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.helpers.PacketHelper;

public class PacketInfoUpdates implements IMessage {

	public NBTTagCompound tag;
	public SyncType type;

	public PacketInfoUpdates() {}

	public PacketInfoUpdates(NBTTagCompound tag, SyncType type) {
		this.tag = tag;
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
		type = SyncType.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
		buf.writeInt(type.ordinal());
	}

	public static class Handler implements IMessageHandler<PacketInfoUpdates, IMessage> {

		@Override
		public IMessage onMessage(PacketInfoUpdates message, MessageContext ctx) {
			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> PacketHelper.receiveInfoUpdate(message.tag, message.type));
			return null;
		}

	}

}
