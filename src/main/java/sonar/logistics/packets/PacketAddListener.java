package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.logistics.PL2;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.helpers.CableHelper;

public class PacketAddListener implements IMessage {

	public ILogicListenable monitor;
	public ListenerType type;

	public PacketAddListener() {}

	public PacketAddListener(ILogicListenable monitor, ListenerType type) {
		this.monitor = monitor;
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		monitor = PL2.getServerManager().getIdentityTile(buf.readInt());
		type = ListenerType.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(monitor.getIdentity());
		buf.writeInt(type.ordinal());
	}

	public static class Handler implements IMessageHandler<PacketAddListener, IMessage> {

		@Override
		public IMessage onMessage(PacketAddListener message, MessageContext ctx) {
			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (message.monitor != null && player != null) {
					message.monitor.getListenerList().addListener(player, message.type);
				}
			});
			return null;
		}
	}

}
