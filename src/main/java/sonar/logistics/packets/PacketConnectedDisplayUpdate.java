package sonar.logistics.packets;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.PL2;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.networking.displays.ConnectedDisplayHandler;

public class PacketConnectedDisplayUpdate implements IMessage {

	public ByteBuf savedBuf;
	public ConnectedDisplay screen;
	public int topLeft;
	public int listSize;
	public List<BlockCoords> connectedCoords;
	public List<Integer> connectedIdentities;
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
		topLeft = buf.readInt();

		listSize = buf.readInt();
		connectedIdentities = Lists.newArrayList();
		connectedCoords = Lists.newArrayList();
		for (int i = 0; i < listSize; i++) {
			connectedIdentities.add(buf.readInt());
			connectedCoords.add(BlockCoords.readFromBuf(buf));
		}

		savedBuf = buf;
		buf.retain();

	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(registryID);
		buf.writeInt(screen.getTopLeftScreen().getIdentity());

		List<ILargeDisplay> displays = ConnectedDisplayHandler.instance().getConnections(registryID);
		buf.writeInt(displays.size());
		displays.forEach(display -> {
			buf.writeInt(display.getIdentity());
			BlockCoords.writeToBuf(buf, display.getCoords());
		});

		screen.writeToBuf(buf);
	}

	public static class Handler implements IMessageHandler<PacketConnectedDisplayUpdate, IMessage> {

		@Override
		public IMessage onMessage(PacketConnectedDisplayUpdate message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					if (message.screen == null) {
						message.screen = PL2.getClientManager().getConnectedDisplays().getOrDefault(message.registryID, new ConnectedDisplay(message.registryID));
					}
					message.screen.readFromBuf(message.savedBuf);
					PL2.getClientManager().getConnectedDisplays().put(message.registryID, message.screen);

					for (int i = 0; i < message.listSize; i++) {
						int iden = message.connectedIdentities.get(i);
						IDisplay display = PL2.getClientManager().getDisplay(iden);
						if (display == null) {
							BlockCoords coords = message.connectedCoords.get(i);
							display = CableHelper.getDisplay(coords.getWorld(), coords.getBlockPos(), EnumDisplayFaceSlot.fromFace(message.screen.getCableFace()));
						}
						if (display != null && display instanceof ILargeDisplay) {
							ILargeDisplay large = ((ILargeDisplay) display);
							large.setRegistryID(message.registryID);
							boolean isTopLeft = iden == message.topLeft;
							large.setShouldRender(isTopLeft);
							if (isTopLeft) {
								message.screen.setTopLeftScreen(large, isTopLeft);
							}
							BlockCoords coords = large.getCoords();
							coords.getWorld().markBlockRangeForRenderUpdate(coords.getBlockPos(), coords.getBlockPos());

						}
					}

					message.savedBuf.release();
				});
			}
			return null;
		}
	}
}