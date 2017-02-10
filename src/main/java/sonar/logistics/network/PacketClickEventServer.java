package sonar.logistics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.logistics.Logistics;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.ScreenInteractionEvent;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IMonitorInfo;

public class PacketClickEventServer implements IMessage {

	public NBTTagCompound eventTag;
	public int hashCode;

	public PacketClickEventServer() {
	}

	public PacketClickEventServer(int hashCode, NBTTagCompound eventTag) {
		this.hashCode = hashCode;
		this.eventTag = eventTag;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		hashCode = buf.readInt();
		eventTag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(hashCode);
		ByteBufUtils.writeTag(buf, eventTag);
	}

	public static class Handler implements IMessageHandler<PacketClickEventServer, IMessage> {

		@Override
		public IMessage onMessage(PacketClickEventServer message, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				SonarCore.proxy.getThreadListener(ctx).addScheduledTask(new Runnable() {
					public void run() {
						EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
						if (player != null) {
							ScreenInteractionEvent event = Logistics.getServerManager().clickEvents.get(message.hashCode);
							if (event != null && event.hit.partHit instanceof IInfoDisplay) {
								InfoContainer container = (InfoContainer) ((IInfoDisplay) event.hit.partHit).container();
								IDisplayInfo displayInfo = container.getDisplayInfo(event.infoPos);
								IMonitorInfo info = displayInfo.getSidedCachedInfo(false);
								if (info != null && info instanceof IAdvancedClickableInfo && info.equals(event.currentInfo)) {
									((IAdvancedClickableInfo) info).onClickEvent(container, displayInfo, event, message.eventTag);
								}
								Logistics.getServerManager().clickEvents.remove(message.hashCode);
							}

						}
					}
				});
			}
			return null;
		}

	}

}
