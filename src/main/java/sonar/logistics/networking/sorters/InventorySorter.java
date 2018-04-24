package sonar.logistics.networking.sorters;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.utils.SortingDirection;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicListSorter;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.values.ItemCount;
import sonar.logistics.api.tiles.readers.ILogicListSorter;
import sonar.logistics.api.tiles.readers.InventoryReader.SortingType;
import sonar.logistics.info.types.MonitoredItemStack;

@LogicListSorter(id = InventorySorter.REGISTRY_NAME, modid = PL2Constants.MODID)
public class InventorySorter implements ILogicListSorter<MonitoredItemStack> {

	public SortingDirection direction;
	public SortingType sorting_type;
	
	public InventorySorter(){}
	
	public InventorySorter(SortingDirection direction, SortingType type){
		this.direction = direction;
		this.sorting_type = type;
	}

	@Override
	public boolean canSort(Object obj) {
		return obj instanceof ItemCount ||obj instanceof MonitoredItemStack;
	}

	@Override
	public AbstractChangeableList<MonitoredItemStack> sortSaveableList(AbstractChangeableList<MonitoredItemStack> updateInfo) {
		return SortingHelper.sortItems(updateInfo, getDirection(), getType());
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		direction = SortingDirection.values()[nbt.getInteger("SdiD")];
		sorting_type = SortingType.values()[nbt.getInteger("StiD")];
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger("SdiD", getDirection().ordinal());
		nbt.setInteger("StiD", getType().ordinal());
		return nbt;
	}
	
	public SortingDirection getDirection(){
		return direction;
	}
	
	public SortingType getType(){
		return sorting_type;
	}	

	public static final String REGISTRY_NAME = "inv_sorter";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
	
}
