package sonar.logistics.api.info;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.cabling.INetworkTile;

import java.util.ArrayList;
import java.util.List;

/** used to identify info and find the monitor which created it */
@Nonnull
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

	public static InfoUUID newEmpty() {
		return new InfoUUID(-1, -1);
	}

	public static InfoUUID newInvalid() {
		return new InfoUUID(-2, -2);
	}

	public int getIdentity() {
		return identity;
	}

	public int getChannelID() {
		return channelID;
	}

	public boolean equals(Object obj) {
		if (obj instanceof InfoUUID) {
			return this.identity == ((InfoUUID) obj).identity && this.channelID == ((InfoUUID) obj).channelID;
		}
		return false;
	}

	public int hashCode() {
		return Objects.hashCode(identity, channelID);
	}

	public static boolean valid(InfoUUID uuid) {
        return uuid != null && uuid.identity >= 0 && uuid.channelID >= 0;
    }

	/** if this uuid has been received from the server yet */
	public static boolean shouldRender(InfoUUID uuid) {
		return uuid.identity != -2;
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

	public static List<InfoUUID> readInfoList(NBTTagCompound nbt, String tagName) {
		List<InfoUUID> newUUIDs = new ArrayList<>();
		NBTTagList tagList = nbt.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			InfoUUID loaded = new InfoUUID();
			loaded.readData(tagList.getCompoundTagAt(i), SyncType.SAVE);
			newUUIDs.add(loaded);
		}
		return newUUIDs;
	}

	public static NBTTagCompound writeInfoList(NBTTagCompound nbt, List<InfoUUID> uuids, String tagName) {
		NBTTagList tagList = new NBTTagList();
		uuids.forEach(obj -> tagList.appendTag(obj.writeData(new NBTTagCompound(), SyncType.SAVE)));
		nbt.setTag(tagName, tagList);
		return nbt;
	}

}
