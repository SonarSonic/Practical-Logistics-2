package sonar.logistics.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;

public class PacketConnectedDisplayUpdate implements IMessage {

	public ByteBuf savedBuf;
	public ConnectedDisplay screen;
	public int registryID;

	public PacketConnectedDisplayUpdate() {}

	public PacketConnectedDisplayUpdate(ConnectedDisplay screen, int registryID) {
		super();
		this.screen = screen;
		this.registryID = registryID;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		registryID = buf.readInt();
		screen = PL2.getClientManager().connectedDisplays.get(registryID);
		savedBuf = buf;

	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(registryID);
		screen.writeToBuf(buf);
	}

	public static class Handler implements IMessageHandler<PacketConnectedDisplayUpdate, IMessage> {

		@Override
		public IMessage onMessage(PacketConnectedDisplayUpdate message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				SonarCore.proxy.getThreadListener(ctx).addScheduledTask(new Runnable() {
					public void run() {
						if (message.screen == null) {
							message.screen = new ConnectedDisplay(message.registryID);
						}
						message.screen.readFromBuf(message.savedBuf);
						PL2.getClientManager().connectedDisplays.put(message.registryID, message.screen);
						ILargeDisplay topLeft = message.screen.getTopLeftScreen();
						if (topLeft != null)
							topLeft.setConnectedDisplay(message.screen);
					}
				});
			}
			return null;
		}
	}
}