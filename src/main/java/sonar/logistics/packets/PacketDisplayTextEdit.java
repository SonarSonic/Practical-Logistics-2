package sonar.logistics.packets;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.SonarCore;
import sonar.core.network.PacketMultipart;
import sonar.core.network.PacketMultipartHandler;
import sonar.logistics.api.tiles.displays.IDisplay;

public class PacketDisplayTextEdit extends PacketMultipart {

	public int infoPosition;
	public ArrayList<String> textList;

	public PacketDisplayTextEdit() {
		super();
	}

	public PacketDisplayTextEdit(int infoPosition, ArrayList<String> textList, int slotID, BlockPos pos) {
		super(slotID, pos);
		this.infoPosition = infoPosition;
		this.textList = textList;
	}

	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);
		infoPosition = buf.readInt();
		textList = new ArrayList<>();
		int maxSize = buf.readInt();
		for (int i = 0; i < maxSize; i++) {
			textList.add(ByteBufUtils.readUTF8String(buf));
		}
	}

	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);
		buf.writeInt(infoPosition);
		buf.writeInt(textList.size());
		textList.forEach(string -> ByteBufUtils.writeUTF8String(buf, string));
	}

	public static class Handler extends PacketMultipartHandler<PacketDisplayTextEdit> {
		@Override
		public IMessage processMessage(PacketDisplayTextEdit message, EntityPlayer player, World world, IMultipartTile part, MessageContext ctx) {
			if (ctx.side == Side.SERVER && part instanceof IDisplay) {
				SonarCore.proxy.getThreadListener(ctx.side).addScheduledTask(() -> {
					//((IDisplay)part).getGSI().getDisplayInfo(message.infoPosition).setFormatStrings(message.textList);
				});
			}
			return null;
		}
	}
}
