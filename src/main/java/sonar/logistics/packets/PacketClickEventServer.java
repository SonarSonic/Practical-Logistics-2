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
import sonar.logistics.PL2;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.IDisplay;

public class PacketClickEventServer implements IMessage {

	public NBTTagCompound clickTag;
	public InfoUUID infoID;
	public DisplayScreenClick click;
	public int identity;
	public int infoPosition;

	public PacketClickEventServer() {}

	public PacketClickEventServer(int identity, int infoPosition, DisplayScreenClick click, InfoUUID infoID, NBTTagCompound clickTag) {
		this.identity = identity;
		this.infoPosition = infoPosition;
		this.click = click;
		this.infoID = infoID;
		this.clickTag = clickTag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		identity = buf.readInt();
		infoPosition = buf.readInt();
		clickTag = ByteBufUtils.readTag(buf);
		click = DisplayScreenClick.readClick(clickTag);
		infoID = InfoUUID.getUUID(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(identity);
		buf.writeInt(infoPosition);
		DisplayScreenClick.writeClick(click, clickTag);
		ByteBufUtils.writeTag(buf, clickTag);
		infoID.writeToBuf(buf);
	}

	public static class Handler implements IMessageHandler<PacketClickEventServer, IMessage> {

		@Override
		public IMessage onMessage(PacketClickEventServer message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (player != null) {
					// SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					IDisplay display = PL2.getServerManager().getDisplay(message.identity);
					if (display != null) {
						InfoContainer container = (InfoContainer) display.container();
						DisplayInfo renderInfo = container.getDisplayMonitoringUUID(message.infoID);
						if (renderInfo != null) {
							IInfo info = PL2.getServerManager().getInfoFromUUID(message.infoID);
							if (info != null && info instanceof IAdvancedClickableInfo)
								((IAdvancedClickableInfo) info).runClickPacket(message.click, container.getDisplayInfo(message.infoPosition), player, message.clickTag);
						}
					}
					// });
				}
			}
			return null;
		}

	}

}
