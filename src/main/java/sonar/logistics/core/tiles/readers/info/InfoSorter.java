package sonar.logistics.core.tiles.readers.info;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.core.tiles.displays.info.types.general.InfoMonitoredValue;
import sonar.logistics.core.tiles.readers.SortingHelper;

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
