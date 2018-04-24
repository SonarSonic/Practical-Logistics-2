package sonar.logistics.networking.sorters;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.values.InfoMonitoredValue;
import sonar.logistics.api.tiles.readers.ILogicListSorter;

public class InfoSorter implements ILogicListSorter<IProvidableInfo> {

	public InfoSorter(){}
	
	@Override
	public boolean canSort(Object obj) {
		return obj instanceof InfoMonitoredValue || obj instanceof IProvidableInfo;
	}

	@Override
	public AbstractChangeableList<IProvidableInfo> sortSaveableList(AbstractChangeableList<IProvidableInfo> updateInfo) {
		return SortingHelper.sortInfo(updateInfo);
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		return nbt;
	}

	public static final String REGISTRY_NAME = "info_sorter";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
