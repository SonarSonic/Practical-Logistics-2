package sonar.logistics.core.tiles.wireless.base;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessConnectionType;
import sonar.logistics.api.core.tiles.wireless.emitters.ClientWirelessEmitter;
import sonar.logistics.base.ClientInfoHandler;

import java.util.ArrayList;
import java.util.List;

public class PacketClientEmitters implements IMessage {

	public EnumWirelessConnectionType type;
	public List<ClientWirelessEmitter> emitters;

	public PacketClientEmitters() {}

	public PacketClientEmitters(EnumWirelessConnectionType type, List<ClientWirelessEmitter> emitters) {
		this.type = type;
		this.emitters = emitters;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		type = EnumWirelessConnectionType.values()[buf.readInt()];
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		emitters = new ArrayList<>();
		if (tag.hasKey("emitters")) {
			NBTTagList tagList = tag.getTagList("emitters", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				emitters.add(NBTHelper.instanceNBTSyncable(ClientWirelessEmitter.class, tagList.getCompoundTagAt(i)));
			}
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type.ordinal());
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList tagList = new NBTTagList();
		emitters.forEach(emitter -> tagList.appendTag(emitter.writeData(new NBTTagCompound(), SyncType.SAVE)));
		if (!tagList.hasNoTags()) {
			tag.setTag("emitters", tagList);
		}
		ByteBufUtils.writeTag(buf, tag);

	}

	public static class Handler implements IMessageHandler<PacketClientEmitters, IMessage> {
		@Override
		public IMessage onMessage(PacketClientEmitters message, MessageContext ctx) {
			SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
				switch(message.type){
				case DATA:
					ClientInfoHandler.instance().clientDataEmitters = message.emitters;
					break;
				case REDSTONE:
					ClientInfoHandler.instance().clientRedstoneEmitters = message.emitters;
					break;
				default:
					break;
				}
			});
			return null;
		}
	}

}
