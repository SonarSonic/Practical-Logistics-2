package sonar.logistics.api.readers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.nodes.BlockConnection;
import sonar.logistics.api.nodes.EntityConnection;
import sonar.logistics.api.nodes.NodeConnection;

public class IdentifiedChannelsList implements ISyncPart {

	public ArrayList<BlockCoords> coordList = new ArrayList();
	public ArrayList<UUID> uuidList = new ArrayList();
	public IUUIDIdentity identity;
	public final int tagID;
	public ISyncableListener listener;
	public ChannelType type;

	public IdentifiedChannelsList(IUUIDIdentity identity, ChannelType type, int tagID) {
		super();
		this.type = type;
		this.tagID = tagID;
		this.identity = identity;
	}

	public IdentifiedChannelsList setListener(ISyncableListener listener) {
		this.listener = listener;
		return this;
	}

	public UUID getIdentity() {
		return identity.getIdentity();
	}

	public boolean equals(Object object) {
		return object != null && (object instanceof IdentifiedChannelsList) ? ((IdentifiedChannelsList) object).identity.equals(identity) : coordList.equals(object);
	}

	public void modifyCoords(BlockCoords coords) {
		if (coords != null) {
			if (!coords.contains(coordList)) {
				if (type == ChannelType.SINGLE) {
					coordList.clear();
					uuidList.clear();
				}
				add(coords);
			} else {
				if (type == ChannelType.SINGLE) {
					coordList.clear();
					uuidList.clear();
				} else {
					Iterator<BlockCoords> iterator = coordList.iterator();
					coordList.removeIf(coord -> coord.equals(coords));
				}
				markDirty();
			}
		}
	}

	public void modifyUUID(UUID newUUID) {
		if (newUUID != null) {
			if (!uuidList.contains(newUUID)) {
				if (type == ChannelType.SINGLE) {
					coordList.clear();
					uuidList.clear();
				}
				addUUID(newUUID);
			} else {
				if (type == ChannelType.SINGLE) {
					coordList.clear();
					uuidList.clear();
				} else {
					Iterator<UUID> iterator = uuidList.iterator();
					uuidList.removeIf(uui -> uui.equals(newUUID));
				}
				markDirty();
			}
		}
	}

	public ArrayList<BlockCoords> getCoords() {
		return coordList;
	}

	public ArrayList<UUID> getUUIDs() {
		return uuidList;
	}

	public boolean hasValidChannels() {
		return type != ChannelType.UNLIMITED ? hasChannels() : true;
	}

	public boolean hasChannels() {
		return uuidList.isEmpty() || coordList.isEmpty();
	}

	public boolean isEntityMonitored(UUID uuid) {
		if (type == ChannelType.UNLIMITED) {
			return ((coordList.isEmpty() && uuidList.isEmpty()) || uuidList.contains(uuid));
		}
		return uuidList.contains(uuid);
	}

	public boolean isCoordsMonitored(BlockCoords coords) {
		if (type == ChannelType.UNLIMITED) {
			return ((coordList.isEmpty() && uuidList.isEmpty()) || coordList.contains(coords));
		}
		return coordList.contains(coords);
	}

	public boolean isMonitored(NodeConnection connection) {
		if (connection instanceof BlockConnection) {
			return isCoordsMonitored(((BlockConnection) connection).coords);
		}
		if (connection instanceof EntityConnection) {
			return isEntityMonitored(((EntityConnection) connection).uuid);
		}
		return false;
	}

	public boolean add(BlockCoords coords) {
		if (!coordList.contains(coords)) {
			boolean add = coordList.add(coords);
			if (add) {
				markDirty();
			}
			return add;
		}
		return false;
	}

	public boolean addAll(Collection<? extends BlockCoords> coords) {
		boolean addAll = false;
		for (BlockCoords coord : coords) {
			boolean add = add(coord);
			if (add) {
				addAll = true;
			}
		}
		return addAll;
	}

	public boolean addUUID(UUID coords) {
		if (!uuidList.contains(coords)) {
			boolean add = uuidList.add(coords);
			if (add) {
				markDirty();
			}
			return add;
		}
		return false;
	}

	public boolean addAllUUID(Collection<? extends UUID> coords) {
		boolean addAll = false;
		for (UUID coord : coords) {
			boolean add = addUUID(coord);
			if (add) {
				addAll = true;
			}
		}
		return addAll;
	}

	public int hashCode() {
		return identity.hashCode();
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, BlockCoords.writeBlockCoords(new NBTTagCompound(), coordList, getTagName()));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		coordList.clear();
		addAll(BlockCoords.readBlockCoords(ByteBufUtils.readTag(buf), getTagName()));
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt = BlockCoords.writeBlockCoords(nbt, coordList, getTagName());
		nbt = writeUUIDS(nbt);
		return nbt;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		coordList = BlockCoords.readBlockCoords(nbt, getTagName());
		uuidList = readUUIDS(nbt);
	}

	public ArrayList<UUID> readUUIDS(NBTTagCompound nbt) {
		ArrayList<UUID> uuids = new ArrayList();
		NBTTagList list = nbt.getTagList("uuids", NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound uuidTag = list.getCompoundTagAt(i);
			uuids.add(uuidTag.getUniqueId("id"));
		}
		return uuids;
	}

	public NBTTagCompound writeUUIDS(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		for (UUID uuid : uuidList) {
			NBTTagCompound uuidTag = new NBTTagCompound();
			uuidTag.setUniqueId("id", uuid);
			list.appendTag(uuidTag);
		}
		nbt.setTag("uuids", list);
		return nbt;
	}

	@Override
	public boolean canSync(SyncType sync) {
		return sync.isType(SyncType.SAVE, SyncType.DEFAULT_SYNC);
	}

	@Override
	public String getTagName() {
		return String.valueOf(tagID);
	}

	@Override
	public ISyncableListener getListener() {
		return listener;
	}

	public void markDirty() {
		if (listener != null)
			listener.markChanged(this);
	}
}
