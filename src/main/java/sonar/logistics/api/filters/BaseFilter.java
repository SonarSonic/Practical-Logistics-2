package sonar.logistics.api.filters;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.DirtyPart;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncableList;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.api.nodes.TransferType;

public abstract class BaseFilter extends DirtyPart implements INodeFilter {

	public SyncableList syncList = new SyncableList(this);
	public SyncTagType.INT hashCode = new SyncTagType.INT(0);
	public SyncEnum<NodeTransferMode> transferMode = new SyncEnum(NodeTransferMode.values(), -1);
	public SyncEnum<FilterList> listType = new SyncEnum(FilterList.values(), -2);

	{
		syncList.addParts(transferMode, hashCode, listType);
	}

	public BaseFilter() {
		this.hashCode.setObject(UUID.randomUUID().hashCode());
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		NBTHelper.readSyncParts(nbt, type, syncList);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTHelper.writeSyncParts(nbt, type, syncList, false);
		return nbt;
	}

	@Override
	public NodeTransferMode getTransferMode() {
		return transferMode.getObject();
	}

	@Override
	public FilterList getListType() {
		return listType.getObject();
	}

	@Override
	public void markChanged(IDirtyPart part) {
		syncList.markSyncPartChanged(part);
		markDirty();
	}

	public int hashCode() {
		return this.hashCode.getObject();
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof BaseFilter) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
}
