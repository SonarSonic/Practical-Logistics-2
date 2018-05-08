package sonar.logistics.network.packets.gsi;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIElementPacketHelper;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;

public class PacketGSIElement implements IMessage {

	public int gsiIdentity, elementIdentity;
	public NBTTagCompound clickTag;

	public PacketGSIElement() {}

	public PacketGSIElement(int gsiIdentity, int elementIdentity, NBTTagCompound clickTag) {
		this.gsiIdentity = gsiIdentity;
		this.elementIdentity = elementIdentity;
		this.clickTag = clickTag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		gsiIdentity = buf.readInt();
		elementIdentity = buf.readInt();
		clickTag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(gsiIdentity);
		buf.writeInt(elementIdentity);
		ByteBufUtils.writeTag(buf, clickTag);
	}

	public static class Handler implements IMessageHandler<PacketGSIElement, IMessage> {

		@Override
		public IMessage onMessage(PacketGSIElement message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
					if (player != null) {
						DisplayGSI gsi = ServerInfoHandler.instance().getGSI(message.gsiIdentity);
						if (gsi != null) {
							IDisplayElement e = gsi.getElementFromIdentity(message.elementIdentity);
							GSIElementPacketHelper.handler.runGSIElementPacket(gsi, e, player, message.clickTag);
						}
					}
				});

			}
			return null;
		}

	}

}
