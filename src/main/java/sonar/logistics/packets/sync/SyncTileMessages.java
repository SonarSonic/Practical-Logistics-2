package sonar.logistics.packets.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncPart;
import sonar.logistics.api.errors.ErrorMessage;
import sonar.logistics.api.tiles.INetworkTile;

import javax.annotation.Nullable;

public class SyncTileMessages extends SyncPart {

	public final INetworkTile tile;
	public byte[] states = new byte[ErrorMessage.values().length];
	public int types;

	public SyncTileMessages(INetworkTile tile, int id) {
		super(id);
		this.tile = tile;
		this.types = ErrorMessage.values().length;
		this.states = new byte[types];
	}

	public boolean isValid(ErrorMessage message) {
		for (ErrorMessage m : tile.getValidMessages()) {
			if (m == message) {
				return true;
			}
		}
		return false;
	}

	public boolean getMessageState(ErrorMessage message) {
		return states[message.ordinal()] != 0;
	}

	public byte getMessageBit(ErrorMessage message) {
		return states[message.ordinal()];
	}

	public void markAllMessages(boolean bool) {
		markAllMessages((byte) (bool ? 1 : 0));
	}

	public void markAllMessages(byte bit) {
		for (ErrorMessage message : ErrorMessage.values())
			if (isValid(message)) {
				states[message.ordinal()] = bit;
			}
	}

	public void markTileMessage(ErrorMessage message, boolean bool) {
		markTileMessage(message, (byte) (bool ? 1 : 0));
	}

	public void markTileMessage(ErrorMessage message, byte bit) {
		if (isValid(message)) {
			states[message.ordinal()] = bit;
		}
	}

	public @Nullable ErrorMessage canOpenGui() {
		for (int i = 0; i < types; i++) {
			if (states[i] > 0 && !ErrorMessage.values()[i].canOpenTile()) {
				return ErrorMessage.values()[i];
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
