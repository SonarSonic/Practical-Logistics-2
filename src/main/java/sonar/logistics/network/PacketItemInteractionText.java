package sonar.logistics.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;

public class PacketItemInteractionText implements IMessage {

	public static ItemStack lastStack;
	public static long lastChanged;

	public ItemStack stack;
	public long stored;
	public long changed;

	public PacketItemInteractionText() {}

	public PacketItemInteractionText(ItemStack stack, long stored, long changed) {
		this.stack = stack;
		this.stored = stored;
		this.changed = changed;

	}

	@Override
	public void fromBytes(ByteBuf buf) {
		stack = ByteBufUtils.readItemStack(buf);
		stored = buf.readLong();
		changed = buf.readLong();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, stack);
		buf.writeLong(stored);
		buf.writeLong(changed);
	}

	public static class Handler implements IMessageHandler<PacketItemInteractionText, IMessage> {
		@Override
		public IMessage onMessage(PacketItemInteractionText message, MessageContext ctx) {
			// RenderInteractionOverlay.setStack(message.stack);
			// RenderInteractionOverlay.stored(message.stored);
			// RenderInteractionOverlay.change(message.changed);

			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(new Runnable() {
				public void run() {
					GuiNewChat chatGui = Minecraft.getMinecraft().ingameGUI.getChatGUI();
					if (lastStack == null || !lastStack.isItemEqual(message.stack)) {
						lastStack = message.stack;
						lastChanged = message.changed;
					} else {
						lastChanged += message.changed;
					}
					chatGui.printChatMessageWithOptionalDeletion(new TextComponentTranslation(TextFormatting.BLUE + "PL2: " + TextFormatting.RESET + "Stored " + message.stored + (lastChanged == 0 ? "" : (lastChanged > 0 ? "" + TextFormatting.GREEN + "+" + lastChanged : "" + TextFormatting.RED + lastChanged)) + TextFormatting.RESET + " x " + lastStack.getDisplayName()), lastStack.getDisplayName().hashCode());
				}
			});
			return null;
		}
	}

}
