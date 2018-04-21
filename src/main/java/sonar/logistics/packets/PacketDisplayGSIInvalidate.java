package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.networking.ClientInfoHandler;

public class PacketDisplayGSIInvalidate implements IMessage {

	public int DISPLAY_ID = -1;
	public int GSI_IDENTITY = -1;
	public boolean CONNECTED = false;

	public PacketDisplayGSIInvalidate() {}

	public PacketDisplayGSIInvalidate(DisplayGSI gsi, IDisplay display) {
		GSI_IDENTITY = gsi.getDisplayGSIIdentity();
		DISPLAY_ID = display.getIdentity();
		CONNECTED = display instanceof ConnectedDisplay;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		GSI_IDENTITY = buf.readInt();
		DISPLAY_ID = buf.readInt();
		CONNECTED = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(GSI_IDENTITY);
		buf.writeInt(DISPLAY_ID);
		buf.writeBoolean(CONNECTED);
	}

	public static class Handler implements IMessageHandler<PacketDisplayGSIInvalidate, IMessage> {

		@Override
		public IMessage onMessage(PacketDisplayGSIInvalidate message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
					if (player != null) {
						IDisplay display = message.CONNECTED ? ClientInfoHandler.instance().getConnectedDisplay(message.DISPLAY_ID) : ClientInfoHandler.instance().displays_tile.get(message.DISPLAY_ID);
						DisplayGSI gsi = display.getGSI();
						if (gsi != null) {
							gsi.invalidate();
						}
					}
				});

			}
			return null;
		}

	}

}
