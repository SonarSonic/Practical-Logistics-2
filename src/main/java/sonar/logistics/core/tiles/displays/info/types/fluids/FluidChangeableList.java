package sonar.logistics.core.tiles.displays.info.types.fluids;

import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.StorageSize;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;

import javax.annotation.Nullable;

public class FluidChangeableList extends AbstractChangeableList<InfoNetworkFluid> {

	public StorageSize sizing = new StorageSize(0, 0);

	public static FluidChangeableList newChangeableList(){
		return new FluidChangeableList();		
	}

	@Override
	public FluidCount createMonitoredValue(InfoNetworkFluid obj) {
		return new FluidCount(obj);
	}

	public void saveStates() {
		super.saveStates();
		sizing = new StorageSize(0, 0);
	}

	public void add(StoredFluidStack stack) {
		FluidCount found = find(stack.fluid);
		if (found == null) {
			values.add(createMonitoredValue(new InfoNetworkFluid(stack)));
		} else {
			found.combine(stack.stored, stack.capacity);
		}
	}

	@Nullable
	public FluidCount find(FluidStack obj) {
		for (IMonitoredValue<InfoNetworkFluid> value : values) {
			FluidCount count = (FluidCount) value;
			if (count.canCombine(obj)) {
				return count;
			}
		}
		return null;
	}

	public long getItemCount(FluidStack stack) {
		for (IMonitoredValue<InfoNetworkFluid> value : getList()) {
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
