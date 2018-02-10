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
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.client.gsi.IGSIPacketHandler;

public class PacketGSIClick implements IMessage {

	public NBTTagCompound clickTag;
	public InfoUUID infoID;
	public DisplayScreenClick click;
	public int infoPosition;

	public PacketGSIClick() {}

	public PacketGSIClick(int infoPosition, DisplayScreenClick click, InfoUUID infoID, NBTTagCompound clickTag) {
		this.infoPosition = infoPosition;
		this.click = click;
		this.infoID = infoID;
		this.clickTag = clickTag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		infoPosition = buf.readInt();
		clickTag = ByteBufUtils.readTag(buf);
		click = DisplayScreenClick.readClick(clickTag);
		infoID = InfoUUID.getUUID(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(infoPosition);
		DisplayScreenClick.writeClick(click, clickTag);
		ByteBufUtils.writeTag(buf, clickTag);
		infoID.writeToBuf(buf);
	}

	public static class Handler implements IMessageHandler<PacketGSIClick, IMessage> {

		@Override
		public IMessage onMessage(PacketGSIClick message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (player != null) {
					SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
						InfoContainer container = PL2.getServerManager().getInfoContainer(message.click.identity);
						if (container != null) {
							if (InfoUUID.valid(message.infoID)) {
								DisplayInfo renderInfo = container.getDisplayMonitoringUUID(message.infoID);
								if (renderInfo != null) {
									IInfo info = PL2.getServerManager().getInfoFromUUID(message.infoID);
									if (info != null) {
										IGSIPacketHandler handler = GSIHelper.getGSIHandler(info);
										handler.runGSIPacket(message.click, container.getDisplayInfo(message.infoPosition), player, message.clickTag);
										return;
									}
								}
							}
							GSIHelper.handler.runGSIPacket(message.click, container.getDisplayInfo(message.infoPosition), player, message.clickTag);
						}
					});
				}
			}
			return null;
		}

	}

}
