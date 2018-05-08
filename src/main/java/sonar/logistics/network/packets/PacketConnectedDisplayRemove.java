package sonar.logistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.logistics.base.ClientInfoHandler;

public class PacketConnectedDisplayRemove implements IMessage {

	public int registryID;

	public PacketConnectedDisplayRemove() {}

	public PacketConnectedDisplayRemove(int registryID) {
		super();
		this.registryID = registryID;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		registryID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(registryID);
	}

	public static class Handler implements IMessageHandler<PacketConnectedDisplayRemove, IMessage> {

		@Override
		public IMessage onMessage(PacketConnectedDisplayRemove message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> ClientInfoHandler.instance().getConnectedDisplays().remove(message.registryID));
			}
			return null;
		}
	}
}