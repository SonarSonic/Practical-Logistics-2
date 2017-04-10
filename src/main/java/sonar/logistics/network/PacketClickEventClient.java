package sonar.logistics.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayInteractionEvent;
import sonar.logistics.common.multiparts.generic.DisplayMultipart;

public class PacketClickEventClient extends PacketMultipart {

	public DisplayInteractionEvent eventTag;
	public ByteBuf buf;

	public PacketClickEventClient() {
	}

	public PacketClickEventClient(UUID partUUID, BlockPos pos, DisplayInteractionEvent eventTag) {
		super(partUUID, pos);
		this.eventTag = eventTag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		this.buf = buf;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		eventTag.writeToBuf(buf);
	}

	public static class Handler extends PacketMultipartHandler<PacketClickEventClient> {

		@Override
		public IMessage processMessage(PacketClickEventClient message, IMultipartContainer target, IMultipart part, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (player != null && part instanceof DisplayMultipart) {
					DisplayInteractionEvent event = DisplayInteractionEvent.readFromBuf(message.buf, player, (DisplayMultipart) part);
					if (event.hit == null) {
						return null;
					}
					InfoContainer container = (InfoContainer) ((DisplayMultipart) part).container();
					if (container != null) {
						IDisplayInfo displayInfo = container.getDisplayInfo(event.infoPos);
						IMonitorInfo info = displayInfo.getSidedCachedInfo(true);
						if (info != null && info instanceof IAdvancedClickableInfo && info.equals(event.currentInfo)) {
							NBTTagCompound eventTag = ((IAdvancedClickableInfo) info).onClientClick(event, displayInfo, player, player.getActiveItemStack(), container);
							return new PacketClickEventServer(event.hashCode, eventTag);
						}
					}
				}
			}
			return null;
		}

	}

}
