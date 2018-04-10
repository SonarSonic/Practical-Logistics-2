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
import sonar.logistics.networking.ClientInfoHandler;

public class PacketDisplayGSIContentsPacket implements IMessage {

	public NBTTagCompound SAVE_TAG;
	public int GSI_IDENTITY = -1;

	public PacketDisplayGSIContentsPacket() {}

	public PacketDisplayGSIContentsPacket(DisplayGSI gsi) {
		GSI_IDENTITY = gsi.getDisplayGSIIdentity();
		SAVE_TAG = gsi.writeData(new NBTTagCompound(), SyncType.SAVE);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		GSI_IDENTITY = buf.readInt();
		SAVE_TAG = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(GSI_IDENTITY);
		ByteBufUtils.writeTag(buf, SAVE_TAG);
	}

	public static class Handler implements IMessageHandler<PacketDisplayGSIContentsPacket, IMessage> {

		@Override
		public IMessage onMessage(PacketDisplayGSIContentsPacket message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (player != null) {
					SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
						DisplayGSI gsi = ClientInfoHandler.instance().getGSI(message.GSI_IDENTITY);
						if (gsi != null) {							
							gsi.readData(message.SAVE_TAG, SyncType.SAVE);
							gsi.validate();
						}
					});
				}
			}
			return null;
		}

	}

}
