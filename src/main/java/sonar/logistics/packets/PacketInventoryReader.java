package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.common.multiparts.readers.TileInventoryReader;
import sonar.logistics.networking.items.ItemHelper;

/** called when the player clicks an item in the inventory reader */
public class PacketInventoryReader extends PacketMultipart {

	public ItemStack selected;
	public int button;

	public PacketInventoryReader() {}

	public PacketInventoryReader(int slotID, BlockPos pos, ItemStack selected, int button) {
		super(slotID, pos);
		this.selected = selected;
		this.button = button;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		if (buf.readBoolean()) {
			this.selected = ByteBufUtils.readItemStack(buf);
		}
		this.button = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		if (selected != null) {
			buf.writeBoolean(true);
			ByteBufUtils.writeItemStack(buf, selected);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeInt(button);
	}

	public static class Handler extends PacketMultipartHandler<PacketInventoryReader> {
		@Override
		public IMessage processMessage(PacketInventoryReader message, EntityPlayer player, World world, IMultipartTile part, MessageContext ctx) {
			if (ctx.side == Side.SERVER) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {					
					if (!(part instanceof TileInventoryReader)) {
						return;
					}
					TileInventoryReader reader = (TileInventoryReader) part;
					ILogisticsNetwork network = reader.getNetwork();
					if (network.isValid()){
						ItemHelper.onNetworkItemInteraction(reader, network, reader.getMonitoredList(), player, message.selected, message.button);
					}					
				});
			}
			return null;
		}
	}
}