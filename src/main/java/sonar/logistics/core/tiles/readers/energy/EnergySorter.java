package sonar.logistics.core.tiles.readers.energy;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.utils.SortingDirection;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMListSorter;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.core.tiles.displays.info.types.energy.MonitoredEnergyStack;
import sonar.logistics.core.tiles.readers.SortingHelper;
import sonar.logistics.core.tiles.readers.energy.EnergyReader.SortingType;

@ASMListSorter(id = EnergySorter.REGISTRY_NAME, modid = PL2Constants.MODID)
public class EnergySorter implements ILogicListSorter<MonitoredEnergyStack> {

	public SortingDirection direction;
	public SortingType sorting_type;
	
	public EnergySorter(){}
	
	public EnergySorter(SortingDirection direction, SortingType type){
		this.direction = direction;
		this.sorting_type = type;
	}

	@Override
	public boolean canSort(Object obj) {
		return (obj instanceof IMonitoredValue && ((IMonitoredValue)obj).getSaveableInfo() instanceof MonitoredEnergyStack) || obj instanceof MonitoredEnergyStack;
	}

	@Override
	public AbstractChangeableList<MonitoredEnergyStack> sortSaveableList(AbstractChangeableList<MonitoredEnergyStack> updateInfo) {
		return SortingHelper.sortEnergy(updateInfo, getDirection(), getType());
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

	public static final String REGISTRY_NAME = "energy_sorter";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
	
}
