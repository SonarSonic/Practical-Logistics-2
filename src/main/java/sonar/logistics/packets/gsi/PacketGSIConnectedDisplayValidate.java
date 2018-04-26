package sonar.logistics.packets.gsi;

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
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.networking.ClientInfoHandler;

public class PacketGSIConnectedDisplayValidate implements IMessage {

	public NBTTagCompound SAVE_TAG;
	public int DISPLAY_ID = -1;
	public int GSI_IDENTITY = -1;

	public PacketGSIConnectedDisplayValidate() {}

	public PacketGSIConnectedDisplayValidate(DisplayGSI gsi, IDisplay display) {
		GSI_IDENTITY = gsi.getDisplayGSIIdentity();
		SAVE_TAG = gsi.writeData(new NBTTagCompound(), SyncType.SAVE);
		DISPLAY_ID = display.getIdentity();	
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		GSI_IDENTITY = buf.readInt();
		SAVE_TAG = ByteBufUtils.readTag(buf);
		DISPLAY_ID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(GSI_IDENTITY);
		ByteBufUtils.writeTag(buf, SAVE_TAG);
		buf.writeInt(DISPLAY_ID);
	}

	public static class Handler implements IMessageHandler<PacketGSIConnectedDisplayValidate, IMessage> {

		@Override
		public IMessage onMessage(PacketGSIConnectedDisplayValidate message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (player != null) {
					SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
						IDisplay display = ClientInfoHandler.instance().getConnectedDisplay(message.DISPLAY_ID);
						DisplayGSI gsi = display == null? null : display.getGSI();
						if(display == null || gsi == null){
							ClientInfoHandler.instance().invalid_gsi.put(message.GSI_IDENTITY, message.SAVE_TAG);
							return;
						}
						gsi.readData(message.SAVE_TAG, SyncType.SAVE);
						gsi.validate();

					});
				}
			}
			return null;
		}

	}

}
