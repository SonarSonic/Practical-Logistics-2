package sonar.logistics.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.api.SonarAPI;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.core.network.PacketStackUpdate;
import sonar.core.network.utils.ByteBufWritable;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.readers.IWirelessStorageReader;
import sonar.logistics.common.multiparts.InventoryReaderPart;
import sonar.logistics.helpers.ItemHelper;

/** called when the player clicks an item in the inventory reader */
public class PacketWirelessStorage implements IMessage {

	public int id;
	public ByteBuf buf;
	public ByteBufWritable[] writables = null;
	public IWirelessStorageReader reader;
	public ItemStack readerStack;
	public EntityPlayer player;

	public PacketWirelessStorage() {
	}

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

			SonarCore.proxy.getThreadListener(ctx).addScheduledTask(new Runnable() {
				public void run() {
					EntityPlayer player = SonarCore.proxy.getPlayerEntity(ctx);
					if (player == null || player.getEntityWorld().isRemote) {
						return;
					}
					ItemStack stack = player.getHeldItemMainhand();
					if (stack.getItem() instanceof IWirelessStorageReader) {
						IWirelessStorageReader reader = (IWirelessStorageReader) stack.getItem();
						reader.readPacket(stack, player, message.buf, message.id);
					}
				}

			});
			return null;
		}
	}
}