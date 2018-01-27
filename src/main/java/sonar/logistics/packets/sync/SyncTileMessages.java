package sonar.logistics.packets.sync;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncPart;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.states.TileMessage;

public class SyncTileMessages extends SyncPart {

	public final INetworkTile tile;
	public byte[] states = new byte[TileMessage.values().length];
	public int types;

	public SyncTileMessages(INetworkTile tile, int id) {
		super(id);
		this.tile = tile;
		this.types = TileMessage.values().length;
		this.states = new byte[types];
	}

	public boolean isValid(TileMessage message) {
		for (TileMessage m : tile.getValidMessages()) {
			if (m == message) {
				return true;
			}
		}
		return false;
	}

	public boolean getMessageState(TileMessage message) {
		return states[message.ordinal()] != 0;
	}

	public byte getMessageBit(TileMessage message) {
		return states[message.ordinal()];
	}

	public void markAllMessages(boolean bool) {
		markAllMessages((byte) (bool ? 1 : 0));
	}

	public void markAllMessages(byte bit) {
		for (TileMessage message : TileMessage.values())
			if (isValid(message)) {
				states[message.ordinal()] = bit;
			}
	}

	public void markTileMessage(TileMessage message, boolean bool) {
		markTileMessage(message, (byte) (bool ? 1 : 0));
	}

	public void markTileMessage(TileMessage message, byte bit) {
		if (isValid(message)) {
			states[message.ordinal()] = bit;
		}
	}

	public @Nullable TileMessage canOpenGui() {
		for (int i = 0; i < types; i++) {
			if (states[i] > 0 && !TileMessage.values()[i].canOpenTile()) {
				return TileMessage.values()[i];
			}
		}
		return null;
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		for (int i = 0; i < types; i++) {
			buf.writeByte(states[i]);
		}
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		for (int i = 0; i < types; i++) {
			states[i] = buf.readByte();
		}
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		nbt.setByteArray(getTagName(), states);

	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		if (nbt.hasKey(getTagName()))
			states = nbt.getByteArray(getTagName());
		return nbt;
	}

}
