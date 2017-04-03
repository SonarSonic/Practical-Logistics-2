package sonar.logistics.common.multiparts;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.info.LogicInfoRegistry;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.ProgressInfo;
import sonar.logistics.network.sync.SyncMonitoredType;

public abstract class LogisticsReader<T extends IProvidableInfo> extends ReaderMultipart<T> {

	private ArrayList<SyncMonitoredType<T>> selected = Lists.newArrayListWithCapacity(getMaxInfo()), paired = Lists.newArrayListWithCapacity(getMaxInfo());
	{
		for (int i = 0; i < getMaxInfo(); i++) {
			selected.add(i, new SyncMonitoredType<T>(i + 10));
			paired.add(i, new SyncMonitoredType<T>(i + 10 + 100));
		}
		syncList.addParts(selected);
		syncList.addParts(paired);
	}

	public LogisticsReader(String handlerID) {
		super(handlerID);
	}

	public LogisticsReader(String handlerID, EnumFacing face) {
		super(handlerID, face);
	}

	public ArrayList<IProvidableInfo> getSelectedInfo() {
		ArrayList<IProvidableInfo> cachedSelected = Lists.<IProvidableInfo>newArrayList();
		selected.forEach(info -> cachedSelected.add(info.getMonitoredInfo()));
		return cachedSelected;
	}

	public ArrayList<IProvidableInfo> getPairedInfo() {
		ArrayList<IProvidableInfo> cachedPaired = Lists.<IProvidableInfo>newArrayList();
		paired.forEach(info -> cachedPaired.add(info.getMonitoredInfo()));
		return cachedPaired;
	}

	// this is kind of messy, could be made better for sure
	public void addInfo(T info, int type, int newPos) {
		ArrayList<SyncMonitoredType<T>> syncInfo = type == 0 ? selected : paired;
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
	public void setMonitoredInfo(MonitoredList<T> updateInfo, ArrayList<NodeConnection> usedChannels, int channelID) {
		ArrayList<IProvidableInfo> cachedSelected = this.getSelectedInfo();
		ArrayList<IProvidableInfo> cachedPaired = this.getPairedInfo();
		for (int i = 0; i < this.getMaxInfo(); i++) {
			InfoUUID id = new InfoUUID(getIdentity().hashCode(), i);
			IProvidableInfo selectedInfo = cachedSelected.get(i);
			IMonitorInfo lastInfo = PL2.getServerManager().info.get(id);
			if (selectedInfo != null) {
				IMonitorInfo latestInfo = selectedInfo;
				Pair<Boolean, IProvidableInfo> newInfo = LogicInfoRegistry.INSTANCE.getLatestInfo(updateInfo, usedChannels, latestInfo);
				if(newInfo.b!=null){
					this.selected.get(i).info = newInfo.b;
				}
				boolean isPair = false;
				if (cachedPaired != null) {
					IProvidableInfo paired = cachedPaired.get(i);
					if (paired != null) {
						Pair<Boolean, IProvidableInfo> newPaired = LogicInfoRegistry.INSTANCE.getLatestInfo(updateInfo, usedChannels, paired);
						if(newPaired.b!=null){
							this.paired.get(i).info = newPaired.b;
						}
						if (newPaired != null && newInfo.b instanceof LogicInfo && newPaired.b instanceof LogicInfo) {
							latestInfo = new ProgressInfo((LogicInfo) newInfo.b, (LogicInfo) newPaired.b);
							isPair = true;
						}
					}
				}
				if (!newInfo.a && lastInfo != null && lastInfo.isMatchingType(newInfo.b) && lastInfo.isMatchingInfo(newInfo.b) && !lastInfo.isIdenticalInfo(newInfo.b)) {
					continue;
				} else if (!isPair) {
					latestInfo = newInfo.b; // FIXME: why was this commented out then?
				}
				PL2.getServerManager().changeInfo(id, latestInfo);
			} else if (lastInfo != null) {
				// set to empty info type
				PL2.getServerManager().changeInfo(id, null);
			}
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
