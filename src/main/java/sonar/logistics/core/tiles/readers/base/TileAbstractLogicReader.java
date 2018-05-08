package sonar.logistics.core.tiles.readers.base;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import sonar.core.utils.Pair;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.core.tiles.displays.info.MasterInfoRegistry;
import sonar.logistics.core.tiles.displays.info.types.progress.InfoProgressBar;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;
import sonar.logistics.network.sync.SyncMonitoredType;

import java.util.List;

public abstract class TileAbstractLogicReader<T extends IProvidableInfo> extends TileAbstractListReader<T> {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK, ErrorMessage.NO_DATA_SELECTED };

	private List<SyncMonitoredType<T>> selected = Lists.newArrayListWithCapacity(getMaxInfo()), paired = Lists.newArrayListWithCapacity(getMaxInfo());
	{
		for (int i = 0; i < getMaxInfo(); i++) {
			selected.add(i, new SyncMonitoredType<>(i + 10));
			paired.add(i, new SyncMonitoredType<>(i + 10 + 100));
		}
		syncList.addParts(selected);
		syncList.addParts(paired);
	}

	public List<T> getCachedInfo(List<SyncMonitoredType<T>> parts) {
		List<T> cached = Lists.newArrayList();
		parts.forEach(i -> cached.add(i.getMonitoredInfo()));
		return cached;
	}

	public List<T> getSelectedInfo() {
		return getCachedInfo(selected);
	}

	public List<T> getPairedInfo() {
		return getCachedInfo(paired);
	}

	// this is kind of messy, could be made better for sure
	public void addInfo(T info, int type, int newPos) {
		List<SyncMonitoredType<T>> syncInfo = type == 0 ? selected : paired;
		if (newPos == -1) {
			int pos = 0;

			for (SyncMonitoredType<T> sync : syncInfo) {
				T pInfo = sync.getMonitoredInfo();
				if (pInfo != null) {
					if (InfoHelper.isMatchingInfo(pInfo, info)) {
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
		} else {
			lastPos = newPos;
		}
		syncInfo.get(newPos == -1 ? 0 : newPos).setInfo(info);
		sendSyncPacket();
	}

	//// ILogicMonitor \\\\
	@Override
	public void setMonitoredInfo(AbstractChangeableList<T> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		List<T> selected = getSelectedInfo();
		List<T> paired = getPairedInfo();
		for (int i = 0; i < getMaxInfo(); i++) {
			IInfo latestInfo = null;
			InfoUUID id = new InfoUUID(getIdentity(), i);
			Pair<Boolean, T> info = MasterInfoRegistry.INSTANCE.getLatestInfo(updateInfo, usedChannels, selected.get(i));
			Pair<Boolean, T> pair = MasterInfoRegistry.INSTANCE.getLatestInfo(updateInfo, usedChannels, paired.get(i));
			if (info != null && info.a) {
				if (pair != null && InfoProgressBar.isStorableInfo(pair.b) && InfoProgressBar.isStorableInfo(info.b)) {
					latestInfo = new InfoProgressBar(info.b, pair.b);
				} else {
					latestInfo = info.b;
				}
			}
			ServerInfoHandler.instance().changeInfo(this, id, latestInfo);
		}
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
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
