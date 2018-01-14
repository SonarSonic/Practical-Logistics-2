package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayInteractionEvent;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public class PacketClickEventClient extends PacketMultipart {

	public DisplayInteractionEvent eventTag;
	public ByteBuf buf;

	public PacketClickEventClient() {}

	public PacketClickEventClient(int slotID, BlockPos pos, DisplayInteractionEvent eventTag) {
		super(slotID, pos);
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
		public IMessage processMessage(PacketClickEventClient message, EntityPlayer player, World world, IMultipartTile part, MessageContext ctx) {
			if (ctx.side == Side.CLIENT) {

				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					if (player != null && part instanceof TileAbstractDisplay) {
						DisplayInteractionEvent event = DisplayInteractionEvent.readFromBuf(message.buf, player, (TileAbstractDisplay) part);
						if (event.hit == null) {
							return;
						}
						InfoContainer container = (InfoContainer) ((TileAbstractDisplay) part).container();
						if (container != null) {
							DisplayInfo displayInfo = container.getDisplayInfo(event.infoPos);
							IInfo info = displayInfo.getSidedCachedInfo(true);
							if (info != null && info instanceof IAdvancedClickableInfo && info.equals(event.currentInfo)) {
								NBTTagCompound eventTag = ((IAdvancedClickableInfo) info).onClientClick(event, displayInfo, player, player.getActiveItemStack(), container);
								if (!eventTag.hasNoTags()) {
									PL2.network.sendToServer(new PacketClickEventServer(event.hashCode, eventTag));
								}
							}

						}
					}
				});
			}
			return null;
		}

	}

}
