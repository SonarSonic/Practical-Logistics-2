package sonar.logistics.api.lists.values;

import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.MonitoredValue;
import sonar.logistics.api.lists.EnumListChange;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.MonitoredValueHelper;
import sonar.logistics.info.types.MonitoredFluidStack;

@MonitoredValue(id = FluidCount.id, modid = PL2Constants.MODID)
public class FluidCount implements IMonitoredValue<MonitoredFluidStack> {

	public static final String id = "fluid_count";
	public MonitoredFluidStack fluid;
	public long stored_old = 0;
	public long capacity_old = 0;
	public boolean isNew;

	public FluidCount(MonitoredFluidStack stack) {
		reset(stack);
		this.isNew = true;
	}

	@Override
	public EnumListChange getChange() {
		if (isNew) {
			return EnumListChange.NEW_VALUE;
		}
		EnumListChange final_change = MonitoredValueHelper.getChange(fluid.getStoredStack().stored, stored_old);
		if (!final_change.shouldDelete() && !final_change.shouldUpdate()) {
			final_change = MonitoredValueHelper.getChange(fluid.getStoredStack().capacity, capacity_old);
		}
		return final_change;
	}

	@Override
	public void resetChange() {
		stored_old = fluid.getStoredStack().stored;
		capacity_old = fluid.getStoredStack().capacity;
		fluid.getStoredStack().stored = 0;
		fluid.getStoredStack().capacity = 0;
		isNew = false;
	}

	@Override
	public void combine(MonitoredFluidStack combine) {
		fluid.getStoredStack().stored += combine.getStoredStack().stored;
		fluid.getStoredStack().capacity += combine.getStoredStack().capacity;
	}

	@Override
	public boolean canCombine(MonitoredFluidStack combine) {
		return fluid.getStoredStack().equalStack(combine.getStoredStack().fluid);
	}

	@Override
	public boolean isValid(Object info) {
		return info instanceof MonitoredFluidStack;
	}

	@Override
	public MonitoredFluidStack getSaveableInfo() {
		return fluid;
	}

	@Override
	public void reset(MonitoredFluidStack fullInfo) {
		fluid = fullInfo.copy();
	}

	@Override
	public void setNew() {
		this.isNew = true;
	}

	@Override
	public boolean shouldDelete(EnumListChange change) {
		return change.shouldDelete() || fluid == null || fluid.getStored() == 0;
	}

}
