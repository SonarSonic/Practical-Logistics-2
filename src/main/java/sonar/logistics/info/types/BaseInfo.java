package sonar.logistics.info.types;

import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.BaseSyncListPart;
import sonar.core.network.sync.ICheckableSyncPart;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.ISyncableListener;
import sonar.core.network.sync.SyncableList;
import sonar.logistics.api.info.IMonitorInfo;

/** typical implementation of IMonitorInfo which has a sync parts list for all the Info things it also has the required constructor which required empty constructor */
public abstract class BaseInfo<T extends IMonitorInfo>extends BaseSyncListPart implements IMonitorInfo<T>, ISyncableListener {

	public BaseInfo() {}

	@Override
	public boolean isHeader() {
		return false;
	}

	public boolean equals(Object object) {
		if (object != null && object instanceof IMonitorInfo) {
			IMonitorInfo info = (IMonitorInfo) object;
			return (info.isHeader() && isHeader()) || (this.isMatchingType(info) && isMatchingInfo((T)info) && isIdenticalInfo((T) info));
		}
		return false;
	}

	@Override
	public void identifyChanges(T newInfo) {
		ArrayList<ISyncPart> parts = syncList.getStandardSyncParts();
		ArrayList<ISyncPart> infoParts = syncList.getStandardSyncParts();	
		
		for(int i=0;i<parts.size();i++){
			ISyncPart toCheck = infoParts.get(i);
			if(toCheck instanceof ICheckableSyncPart){
				if(!((ICheckableSyncPart) parts.get(i)).equalPart(toCheck)){
					toCheck.getListener().markChanged(toCheck);
				}
			}else{
				toCheck.getListener().markChanged(toCheck);
			}
		}
	}

}
