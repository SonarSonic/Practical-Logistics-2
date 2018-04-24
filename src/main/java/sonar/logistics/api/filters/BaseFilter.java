package sonar.logistics.api.filters;

import java.util.UUID;

import sonar.core.network.sync.BaseSyncListPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;

public abstract class BaseFilter extends BaseSyncListPart implements INodeFilter {

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
	public NodeTransferMode getTransferMode() {
		return transferMode.getObject();
	}

	@Override
	public FilterList getListType() {
		return listType.getObject();
	}

	public int hashCode() {
		return this.hashCode.getObject();
	}

	public boolean equals(Object obj) {
		if (obj instanceof BaseFilter) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	public boolean shouldEmbed() {
		return false;
	}
}
