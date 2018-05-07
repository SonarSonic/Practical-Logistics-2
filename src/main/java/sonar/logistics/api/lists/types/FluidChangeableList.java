package sonar.logistics.api.lists.types;

import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.StorageSize;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.values.FluidCount;
import sonar.logistics.info.types.MonitoredFluidStack;

import javax.annotation.Nullable;

public class FluidChangeableList extends AbstractChangeableList<MonitoredFluidStack> {

	public StorageSize sizing = new StorageSize(0, 0);

	public static FluidChangeableList newChangeableList(){
		return new FluidChangeableList();		
	}

	@Override
	public FluidCount createMonitoredValue(MonitoredFluidStack obj) {
		return new FluidCount(obj);
	}

	public void saveStates() {
		super.saveStates();
		sizing = new StorageSize(0, 0);
	}

	public void add(StoredFluidStack stack) {
		FluidCount found = find(stack.fluid);
		if (found == null) {
			values.add(createMonitoredValue(new MonitoredFluidStack(stack)));
		} else {
			found.combine(stack.stored, stack.capacity);
		}
	}

	@Nullable
	public FluidCount find(FluidStack obj) {
		for (IMonitoredValue<MonitoredFluidStack> value : values) {
			FluidCount count = (FluidCount) value;
			if (count.canCombine(obj)) {
				return count;
			}
		}
		return null;
	}

	public long getItemCount(FluidStack stack) {
		for (IMonitoredValue<MonitoredFluidStack> value : getList()) {
			if (value instanceof FluidCount) {
				FluidCount count = (FluidCount) value;
				if (count.fluid.getStoredStack().equalStack(stack)) {
					return count.fluid.getStored();
				}
			}
		}
		return 0;
	}
}
