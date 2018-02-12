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
import sonar.logistics.api.displays.DisplayGSI;
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
	public DisplayScreenClick click;
	public int elementIdentity;

	public PacketGSIClick() {}

	public PacketGSIClick(int elementIdentity, DisplayScreenClick click, NBTTagCompound clickTag) {
		this.elementIdentity = elementIdentity;
		this.click = click;
		this.clickTag = clickTag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		elementIdentity = buf.readInt();
		clickTag = ByteBufUtils.readTag(buf);
		click = DisplayScreenClick.readClick(clickTag);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(elementIdentity);
		DisplayScreenClick.writeClick(click, clickTag);
		ByteBufUtils.writeTag(buf, clickTag);
	}

	public static class Handler implements IMessageHandler<PacketGSIClick, IMessage> {

		@Override
		public IMessage onMessage(PacketGSIClick message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (player != null) {
					SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
						// FIXME ELEMENT IDENTITY
						DisplayGSI gsi = PL2.getServerManager().getDisplayGSI(message.click.identity);
						if (gsi != null) {
							message.click.gsi = gsi;
							GSIHelper.handler.runGSIPacket(gsi, message.click, player, message.clickTag);
						}
					});
				}
			}
			return null;
		}

	}

}
