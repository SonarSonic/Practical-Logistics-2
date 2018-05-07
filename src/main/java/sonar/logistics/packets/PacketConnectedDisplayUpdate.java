package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.displays.tiles.ConnectedDisplay;
import sonar.logistics.api.displays.tiles.EnumDisplayFaceSlot;
import sonar.logistics.api.displays.tiles.IDisplay;
import sonar.logistics.api.displays.tiles.ILargeDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.networking.ClientInfoHandler;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.networking.displays.DisplayHandler;

import java.util.ArrayList;
import java.util.List;

public class PacketConnectedDisplayUpdate implements IMessage {

	public NBTTagCompound saved;
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
		connectedIdentities = new ArrayList<>();
		connectedCoords = new ArrayList<>();
		for (int i = 0; i < listSize; i++) {
			connectedIdentities.add(buf.readInt());
			connectedCoords.add(BlockCoords.readFromBuf(buf));
		}
		saved = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(registryID);
		buf.writeInt(screen.getTopLeftScreen().getIdentity());

		List<ILargeDisplay> displays = DisplayHandler.instance().getConnections(registryID);
		buf.writeInt(displays.size());
		displays.forEach(display -> {
			buf.writeInt(display.getIdentity());
			BlockCoords.writeToBuf(buf, display.getCoords());
		});
		ByteBufUtils.writeTag(buf, screen.writeData(new NBTTagCompound(), SyncType.SAVE));
	}

	public static class Handler implements IMessageHandler<PacketConnectedDisplayUpdate, IMessage> {

		@Override
		public IMessage onMessage(PacketConnectedDisplayUpdate message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> doMessage(message, ctx));
			}
			return null;
		}

		public static void doMessage(PacketConnectedDisplayUpdate message, MessageContext ctx) {

			World world = SonarCore.proxy.getPlayerEntity(ctx).getEntityWorld();
			if (message.screen == null) {
				ClientInfoHandler.instance().getConnectedDisplays().putIfAbsent(message.registryID, ConnectedDisplay.loadDisplay(world, message.registryID));
				message.screen = ClientInfoHandler.instance().getConnectedDisplays().get(message.registryID);				
			}
			message.screen.readData(message.saved, SyncType.SAVE);		
			message.screen.getGSI().updateScaling();
			if(!message.screen.getGSI().isValid()){
				message.screen.getGSI().validate();
			}
			
			ClientInfoHandler.instance().getConnectedDisplays().put(message.registryID, message.screen);
			for (int i = 0; i < message.listSize; i++) {
				int iden = message.connectedIdentities.get(i);
				IDisplay display = ClientInfoHandler.instance().displays_tile.get(iden);
				if (display == null) {
					BlockCoords coords = message.connectedCoords.get(i);
					display = CableHelper.getDisplay(world, coords.getBlockPos(), EnumDisplayFaceSlot.fromFace(message.screen.getCableFace()));
					ClientInfoHandler.instance().displays_tile.put(iden, display);
				}
				if (display instanceof TileLargeDisplayScreen) {
					TileLargeDisplayScreen large = ((TileLargeDisplayScreen) display);
					large.identity = iden;
					large.setRegistryID(message.registryID);
					boolean isTopLeft = iden == message.topLeft;
					large.setShouldRender(isTopLeft);
					if (isTopLeft) {
						message.screen.setTopLeftScreen(large, true);
					}
					BlockCoords coords = large.getCoords();
					world.markBlockRangeForRenderUpdate(coords.getBlockPos(), coords.getBlockPos());
				}
			}

		}
	}
}