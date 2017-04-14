package sonar.logistics.common.multiparts.readers;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.ProgressInfo;
import sonar.logistics.network.sync.SyncMonitoredType;

public abstract class AbstractInfoReaderPart<T extends IProvidableInfo> extends AbstractListReaderPart<T> {

	private List<SyncMonitoredType<T>> selected = Lists.newArrayListWithCapacity(getMaxInfo()), paired = Lists.newArrayListWithCapacity(getMaxInfo());
	{
		for (int i = 0; i < getMaxInfo(); i++) {
			selected.add(i, new SyncMonitoredType<T>(i + 10));
			paired.add(i, new SyncMonitoredType<T>(i + 10 + 100));
		}
		syncList.addParts(selected);
		syncList.addParts(paired);
	}

	public List<IProvidableInfo> getSelectedInfo() {
		List<IProvidableInfo> cachedSelected = Lists.<IProvidableInfo>newArrayList();
		selected.forEach(info -> cachedSelected.add(info.getMonitoredInfo()));
		return cachedSelected;
	}

	public List<IProvidableInfo> getPairedInfo() {
		List<IProvidableInfo> cachedPaired = Lists.<IProvidableInfo>newArrayList();
		paired.forEach(info -> cachedPaired.add(info.getMonitoredInfo()));
		return cachedPaired;
	}

	// this is kind of messy, could be made better for sure
	public void addInfo(T info, int type, int newPos) {
		List<SyncMonitoredType<T>> syncInfo = type == 0 ? selected : paired;
		if (newPos == -1) {
			int pos = 0;
			for (SyncMonitoredType<T> sync : syncInfo) {
				if (sync.getMonitoredInfo() != null) {
					if (sync.getMonitoredInfo().isMatchingType(info) && sync.getMonitoredInfo().isMatchingInfo(info)) {
						sync.setInfo(null);
						(type != 0 ? selected : paired).get(pos).setInfo(null);
						sendByteBufPacket(100);
						lastPos = -1;
						return;
					}
				} else if (newPos == -1) {
					newPos = pos;
				}
				pos++;
			}
		}
		if (newPos != -1) {
			lastPos = newPos;
		}
		syncInfo.get(newPos == -1 ? 0 : newPos).setInfo(info);
		sendSyncPacket();
		// sendByteBufPacket(100);
	}

	//// ILogicMonitor \\\\
	@Override
	public void setMonitoredInfo(MonitoredList<T> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		List<IProvidableInfo> selected = this.getSelectedInfo();
		List<IProvidableInfo> paired = this.getPairedInfo();
		for (int i = 0; i < this.getMaxInfo(); i++) {
			IInfo latestInfo = null;
			InfoUUID id = new InfoUUID(getIdentity(), i);
			Pair<Boolean, IProvidableInfo> info = LogicInfoRegistry.INSTANCE.getLatestInfo(updateInfo, usedChannels, selected.get(i));
			Pair<Boolean, IProvidableInfo> pair = LogicInfoRegistry.INSTANCE.getLatestInfo(updateInfo, usedChannels, paired.get(i));
			if (info != null && info.a) {
				if (pair!=null && pair.b != null && info.b instanceof LogicInfo && pair.b instanceof LogicInfo) {
					latestInfo = new ProgressInfo((LogicInfo) info.b, (LogicInfo) pair.b);
				} else {
					latestInfo = info != null ? info.b : InfoError.noData;
				}
				//this.selected.get(i).info = info.b;
				//this.paired.get(i).info = pair.b;
			}
			PL2.getServerManager().changeInfo(id, latestInfo);
		}
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		if (id == 100) {
			for (SyncMonitoredType<T> sync : selected) {
				sync.writeToBuf(buf);
			}
			for (SyncMonitoredType<T> sync : paired) {
				sync.writeToBuf(buf);
			}
		}
		switch (id) {
		case ADD:
		case PAIRED:
			selectedInfo.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		if (id == 100) {
			for (SyncMonitoredType<T> sync : selected) {
				sync.readFromBuf(buf);
			}
			for (SyncMonitoredType<T> sync : paired) {
				sync.readFromBuf(buf);
			}
		}
		switch (id) {
		case PAIRED:
			selectedInfo.readFromBuf(buf);
			addInfo((T) selectedInfo.info, 1, lastPos);
			break;
		case ADD:
			selectedInfo.readFromBuf(buf);
			addInfo((T) selectedInfo.info, 0, -1);
			break;
		}

	}
}
