package sonar.logistics.api.info;

import com.google.common.base.Objects;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.viewers.ILogicListenable;

/** used to identify info and find the monitor which created it */
public class InfoUUID implements INBTSyncable {

	public int identity;
	public int channelID;

	public InfoUUID() {}

	public InfoUUID(INetworkTile tile, int channelID) {
		this.identity = tile.getIdentity();
		this.channelID = channelID;
	}
	
	public InfoUUID(int identity, int channelID) {
		this.identity = identity;
		this.channelID = channelID;
	}

	public int getIdentity() {
		return identity;
	}

	public int getChannelID() {
		return channelID;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof InfoUUID) {
			return this.identity == ((InfoUUID) obj).identity && this.channelID == ((InfoUUID) obj).channelID;
		}
		return false;
	}

	public int hashCode() {
		return Objects.hashCode(identity, channelID);
	}

	public boolean valid() {
		if (identity == -1 && channelID == -1) {
			return false;
		}
		return true;
	}

	public static InfoUUID getUUID(ByteBuf buf) {
		return new InfoUUID(buf.readInt(), buf.readInt());
	}

	public void writeToBuf(ByteBuf buf) {
		buf.writeInt(identity);
		buf.writeInt(channelID);
	}

	public String toString() {
		return identity + ":" + channelID;
	}

	public static InfoUUID fromString(String string) {
		String[] ids = string.split(":");
		return new InfoUUID(Integer.valueOf(ids[0]), Integer.valueOf(ids[1]));
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		identity = nbt.getInteger("hash");
		channelID = nbt.getInteger("pos");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger("hash", identity);
		nbt.setInteger("pos", channelID);
		return nbt;
	}
}
