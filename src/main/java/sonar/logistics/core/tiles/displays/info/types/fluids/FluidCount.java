package sonar.logistics.core.tiles.displays.info.types.fluids;

import net.minecraftforge.fluids.FluidStack;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.lists.EnumListChange;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;

@ASMMonitoredValue(id = FluidCount.id, modid = PL2Constants.MODID)
public class FluidCount implements IMonitoredValue<InfoNetworkFluid> {

	public static final String id = "fluid_count";
	public InfoNetworkFluid fluid;
	public long stored_old = 0;
	public long capacity_old = 0;
	public boolean isNew;

	public FluidCount(InfoNetworkFluid stack) {
		reset(stack);
		this.isNew = true;
	}

	@Override
	public EnumListChange getChange() {
		if (isNew) {
			return EnumListChange.NEW_VALUE;
		}
		EnumListChange final_change = EnumListChange.getChange(fluid.getStoredStack().stored, stored_old);
		if (!final_change.shouldDelete() && !final_change.shouldUpdate()) {
			final_change = EnumListChange.getChange(fluid.getStoredStack().capacity, capacity_old);
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
	public void combine(InfoNetworkFluid combine) {
		fluid.getStoredStack().stored += combine.getStoredStack().stored;
		fluid.getStoredStack().capacity += combine.getStoredStack().capacity;
	}

	public void combine(long stored, long capacity) {
		fluid.getStoredStack().stored += stored;
		fluid.getStoredStack().capacity += capacity;
	}

	@Override
	public boolean canCombine(InfoNetworkFluid combine) {
		return fluid.getStoredStack().equalStack(combine.getStoredStack().fluid);
	}

	public boolean canCombine(FluidStack combine) {
		return fluid.getStoredStack().equalStack(combine);
	}

	@Override
	public boolean isValid(Object info) {
		return info instanceof InfoNetworkFluid;
	}

	@Override
	public InfoNetworkFluid getSaveableInfo() {
		return fluid;
	}

	@Override
	public void reset(InfoNetworkFluid fullInfo) {
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
