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
import sonar.logistics.api.displays.storage.DisplayGSISaveHandler;
import sonar.logistics.networking.ClientInfoHandler;

public class PacketGSISavedDataPacket implements IMessage {

	public NBTTagCompound SAVE_TAG;
	public int GSI_IDENTITY = -1;
	public DisplayGSISaveHandler.DisplayGSISavedData saveType;

	public PacketGSISavedDataPacket() {}

	public PacketGSISavedDataPacket(DisplayGSI gsi, DisplayGSISaveHandler.DisplayGSISavedData saveType) {
		GSI_IDENTITY = gsi.getDisplayGSIIdentity();
		SAVE_TAG = DisplayGSISaveHandler.writeGSIData(gsi, new NBTTagCompound(), SyncType.SAVE, saveType);
		this.saveType = saveType;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		GSI_IDENTITY = buf.readInt();
		SAVE_TAG = ByteBufUtils.readTag(buf);
		saveType = DisplayGSISaveHandler.DisplayGSISavedData.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(GSI_IDENTITY);
		ByteBufUtils.writeTag(buf, SAVE_TAG);
		buf.writeInt(saveType.ordinal());
	}

	public static class Handler implements IMessageHandler<PacketGSISavedDataPacket, IMessage> {

		@Override
		public IMessage onMessage(PacketGSISavedDataPacket message, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
					if (player != null) {
						DisplayGSI gsi = ClientInfoHandler.instance().getGSI(message.GSI_IDENTITY);
						if (gsi != null) {
							DisplayGSISaveHandler.readGSIData(gsi, message.SAVE_TAG, SyncType.SAVE, message.saveType);
							gsi.validate();
						}
					}
				});
			}
			return null;
		}

	}

}
