package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.api.displays.tiles.IDisplay;
import sonar.logistics.api.info.InfoUUID;

public class PacketLocalProviderSelection extends PacketMultipart {

	public int infoPosition;
	public InfoUUID uuid;

	public PacketLocalProviderSelection() {
		super();
	}

	public PacketLocalProviderSelection(int infoPosition, InfoUUID uuid, int slotID, BlockPos pos) {
		super(slotID, pos);
		this.infoPosition = infoPosition;
		this.uuid = uuid;
	}

	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		infoPosition = buf.readInt();
		uuid = InfoUUID.getUUID(buf);
	}

	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(infoPosition);
		uuid.writeToBuf(buf);
	}

	public static class Handler extends PacketMultipartHandler<PacketLocalProviderSelection> {
		@Override
		public IMessage processMessage(PacketLocalProviderSelection message, EntityPlayer player, World world, IMultipartTile part, MessageContext ctx) {
			if (ctx.side == Side.SERVER && part instanceof IDisplay) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					//FIXME do we still need this packet???  LocalProviderHandler.doLocalProviderPacket((IDisplay) part, message.uuid, message.infoPosition);
				});
			}
			return null;
		}
	}
}
