package sonar.logistics.packets.gsi;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.displays.TileDisplayScreen;

public class PacketGSIStandardDisplayValidate extends PacketMultipart {

	public NBTTagCompound SAVE_TAG;
	public int GSI_IDENTITY = -1;

	public PacketGSIStandardDisplayValidate() {}

	public PacketGSIStandardDisplayValidate(TileAbstractDisplay display, DisplayGSI gsi) {
		super(display.getSlotID(), display.getPos());
		GSI_IDENTITY = gsi.getDisplayGSIIdentity();
		SAVE_TAG = gsi.writeData(new NBTTagCompound(), SyncType.SAVE);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		GSI_IDENTITY = buf.readInt();
		SAVE_TAG = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(GSI_IDENTITY);
		ByteBufUtils.writeTag(buf, SAVE_TAG);
	}

	public static class Handler extends PacketMultipartHandler<PacketGSIStandardDisplayValidate> {

		@Override
		public IMessage processMessage(PacketGSIStandardDisplayValidate message, EntityPlayer player, World world, IMultipartTile part, MessageContext ctx) {
			if (ctx.side == Side.CLIENT && part instanceof TileDisplayScreen) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					TileDisplayScreen display = (TileDisplayScreen) part;
					DisplayGSI gsi = display.getGSI();
					if(gsi == null){
						gsi = display.container = new DisplayGSI(display, display.getActualWorld(), display.getInfoContainerID());
					}
					gsi.readData(message.SAVE_TAG, SyncType.SAVE);
					gsi.validate();

				});
			}
			return null;
		}

	}

}
