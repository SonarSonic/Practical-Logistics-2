package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.network.utils.ByteBufWritable;
import sonar.logistics.api.tiles.readers.IWirelessStorageReader;

/** called when the player clicks an item in the inventory reader */
public class PacketWirelessStorage implements IMessage {

	public int id;
	public ByteBuf buf;
	public ByteBufWritable[] writables = null;
	public IWirelessStorageReader reader;
	public ItemStack readerStack;
	public EntityPlayer player;

	public PacketWirelessStorage() {}

	public PacketWirelessStorage(IWirelessStorageReader reader, ItemStack readerStack, EntityPlayer player, int id, ByteBufWritable... writables) {
		super();
		this.reader = reader;
		this.readerStack = readerStack;
		this.player = player;
		this.id = id;
		this.writables = writables;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.id = buf.readInt();
		this.buf = buf;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		if (writables != null) {
			boolean replaces = false;
			for (ByteBufWritable writable : writables) {
				writable.writeToBuf(buf);
				if (writable.replacesDefaults) {
					replaces = true;
				}
			}
			if (replaces) {
				return;
			}
		}
		reader.writePacket(readerStack, player, buf, id);
	}

	public static class Handler implements IMessageHandler<PacketWirelessStorage, IMessage> {
		@Override
		public IMessage onMessage(PacketWirelessStorage message, MessageContext ctx) {

			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
				EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
				if (player == null || player.getEntityWorld().isRemote) {
					return;
				}
				ItemStack stack = player.getHeldItemMainhand();
				if (stack.getItem() instanceof IWirelessStorageReader) {
					IWirelessStorageReader reader = (IWirelessStorageReader) stack.getItem();
					reader.readPacket(stack, player, message.buf, message.id);
				}
			});
			return null;
		}
	}
}