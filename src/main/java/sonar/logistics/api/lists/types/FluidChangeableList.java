package sonar.logistics.api.lists.types;

import sonar.core.api.StorageSize;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.values.FluidCount;
import sonar.logistics.info.types.MonitoredFluidStack;

public class FluidChangeableList extends AbstractChangeableList<MonitoredFluidStack> {

	public StorageSize sizing = new StorageSize(0, 0);// FIXME

	public static final FluidChangeableList newChangeableList(){
		return new FluidChangeableList();		
	}

	@Override
	public FluidCount createMonitoredValue(MonitoredFluidStack obj) {
		return new FluidCount(obj);
	}

	public void doCombine(FluidCount value, MonitoredFluidStack obj) {
		super.doCombine(value, obj);
	}

	public void saveStates() {
		super.saveStates();
		sizing = new StorageSize(0, 0);
	}
}
