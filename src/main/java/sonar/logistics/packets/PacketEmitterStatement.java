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
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.logistics.api.tiles.signaller.EmitterStatement;
import sonar.logistics.api.tiles.signaller.ILogisticsTile;
import sonar.logistics.api.utils.ListPacket;

import java.util.Collections;

/** called when the player clicks an item in the inventory reader */
public class PacketEmitterStatement extends PacketMultipart {

	public EmitterStatement statement;
	public ListPacket packetType;

	public PacketEmitterStatement() {}

	public PacketEmitterStatement(int slotID, BlockPos pos, ListPacket packetType) {
		super(slotID, pos);
		this.packetType = packetType;
	}

	public PacketEmitterStatement(int slotID, BlockPos pos, ListPacket packetType, EmitterStatement filter) {
		super(slotID, pos);
		this.statement = filter;
		this.packetType = packetType;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		if (buf.readBoolean()) {
			statement = NBTHelper.instanceNBTSyncable(EmitterStatement.class, ByteBufUtils.readTag(buf));
		}
		packetType = ListPacket.values()[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeBoolean(statement != null);
		if (statement != null) {
			ByteBufUtils.writeTag(buf, statement.writeData(new NBTTagCompound(), SyncType.SAVE));
		}
		buf.writeInt(packetType.ordinal());
	}

	public static class Handler extends PacketMultipartHandler<PacketEmitterStatement> {
		@Override
		public IMessage processMessage(PacketEmitterStatement message, EntityPlayer player, World world, IMultipartTile part, MessageContext ctx) {
			if (player == null || player.getEntityWorld().isRemote || !(part instanceof ILogisticsTile)) {
				return null;
			}
			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
				ILogisticsTile tile = (ILogisticsTile) part;
				SyncNBTAbstractList<EmitterStatement> filters = tile.getStatements();
				switch (message.packetType) {
				case ADD:
					for (EmitterStatement filter : filters.getObjects()) {
						if (filter.equals(message.statement)) {
							filter.readData(message.statement.writeData(new NBTTagCompound(), SyncType.SAVE), SyncType.SAVE);
							filters.markChanged();
							return;
						}
					}
					filters.addObject(message.statement);
					break;
				case MOVE_DOWN:

					int listPos = -1;
					for (int i = 0; i < filters.objs.size(); i++) {
						EmitterStatement filter = filters.getObjects().get(i);
						if (filter.equals(message.statement)) {
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
						EmitterStatement filter = filters.getObjects().get(i);
						if (filter.equals(message.statement)) {
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
					filters.removeObject(message.statement);
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