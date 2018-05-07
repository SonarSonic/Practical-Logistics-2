package sonar.logistics.packets;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.api.filters.IFilteredTile;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.utils.ListPacket;
import sonar.logistics.networking.info.InfoHelper;
import sonar.logistics.packets.sync.SyncFilterList;

import java.util.Collections;

/** called when the player clicks an item in the inventory reader */
public class PacketNodeFilter extends PacketMultipart {

	public INodeFilter filter;
	public ListPacket packetType;

	public PacketNodeFilter() {}

	public PacketNodeFilter(int slotID, BlockPos pos, ListPacket packetType) {
		super(slotID, pos);
		this.packetType = packetType;
	}

	public PacketNodeFilter(int slotID, BlockPos pos, ListPacket packetType, INodeFilter filter) {
		super(slotID, pos);
		this.filter = filter;
		this.packetType = packetType;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		if (buf.readBoolean()) {
			filter = InfoHelper.readFilterFromNBT(ByteBufUtils.readTag(buf));
		}
		packetType = ListPacket.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(filter != null);
		if (filter != null) {
			ByteBufUtils.writeTag(buf, InfoHelper.writeFilterToNBT(new NBTTagCompound(), filter, SyncType.SAVE));
		}
		buf.writeInt(packetType.ordinal());
	}

	public static class Handler extends PacketMultipartHandler<PacketNodeFilter> {
		@Override
		public IMessage processMessage(PacketNodeFilter message, EntityPlayer player, World world, IMultipartTile part, MessageContext ctx) {
			if (player == null || player.getEntityWorld().isRemote || !(part instanceof IFilteredTile)) {
				return null;
			}
			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
				IFilteredTile tile = (IFilteredTile) part;
				SyncFilterList filters = tile.getFilters();
				switch (message.packetType) {
				case ADD:
					for (INodeFilter filter : filters.getObjects()) {
						if (filter.equals(message.filter)) {
							filter.readData(message.filter.writeData(new NBTTagCompound(), SyncType.SAVE), SyncType.SAVE);
							filters.markChanged();
							return;
						}
					}
					filters.addObject(message.filter);
					break;
				case MOVE_DOWN:

					int listPos = -1;
					for (int i = 0; i < filters.objs.size(); i++) {
						INodeFilter filter = filters.getObjects().get(i);
						if (filter.equals(message.filter)) {
							listPos = i;
						}
					}
					if (listPos + 1 > 0) {
						if (listPos + 1 < filters.objs.size()) {
							Collections.swap(filters.objs, listPos, listPos + 1);
							filters.markChanged();
						}
					}
					break;
				case MOVE_UP:
					listPos = -1;
					for (int i = 0; i < filters.objs.size(); i++) {
						INodeFilter filter = filters.getObjects().get(i);
						if (filter.equals(message.filter)) {
							listPos = i;
						}
					}
					if (listPos - 1 >= 0) {
						if (listPos - 1 < filters.objs.size()) {
							Collections.swap(filters.objs, listPos, listPos - 1);
							filters.markChanged();
						}
					}
					break;
				case REMOVE:
					filters.removeObject(message.filter);
					break;
				case CLEAR:
					filters.objs.clear();
					filters.markChanged();
					break;
				default:
					break;

				}

			});

			return null;
		}
	}
}