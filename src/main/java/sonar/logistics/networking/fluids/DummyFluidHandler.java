package sonar.logistics.networking.fluids;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import sonar.core.api.SonarAPI;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.utils.ActionType;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;

/**a dummy Fluid Handler to make use of the FluidUtil handling methods*/
public class DummyFluidHandler implements IFluidHandler, IFluidTankProperties {

	public ILogisticsNetwork network;
	public StoredFluidStack fluid;

	public DummyFluidHandler(ILogisticsNetwork network, StoredFluidStack fluid) {
		this.network = network;
		this.fluid = fluid;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] { this };
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		StoredFluidStack toFill = new StoredFluidStack(resource);
		StoredFluidStack stack = FluidHelper.transferFluids(network, toFill.copy(), NodeTransferMode.ADD, ActionType.getTypeForAction(!doFill), null);
		StoredFluidStack returned = SonarAPI.getFluidHelper().getStackToAdd(resource.amount, toFill, stack);
		return returned == null ? 0 : returned.getFullStack().amount;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		StoredFluidStack toDrain = new StoredFluidStack(resource);
		StoredFluidStack stack = FluidHelper.transferFluids(network, toDrain.copy(), NodeTransferMode.REMOVE, ActionType.getTypeForAction(!doDrain), null);
		StoredFluidStack returned = SonarAPI.getFluidHelper().getStackToAdd(resource.amount, toDrain, stack);
		return returned == null ? null : returned.getFullStack();
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		StoredFluidStack toDrain = new StoredFluidStack(fluid.getFullStack(), maxDrain, maxDrain);
		StoredFluidStack stack = FluidHelper.transferFluids(network, toDrain.copy(), NodeTransferMode.REMOVE, ActionType.getTypeForAction(!doDrain), null);
		StoredFluidStack returned = SonarAPI.getFluidHelper().getStackToAdd(maxDrain, toDrain, stack);
		return returned == null ? null : returned.getFullStack();
	}

	//// IFluidTankProperties \\\\

	@Override
	public FluidStack getContents() {
		return fluid == null ? null : fluid.getFullStack();
	}

	@Override
	public int getCapacity() {
		return (int) Math.min(Integer.MAX_VALUE, fluid == null ? Integer.MAX_VALUE : fluid.capacity);
	}

	@Override
	public boolean canFill() {
		return true;// fluid.stored < fluid.capacity;
	}

	@Override
	public boolean canDrain() {
		return fluid!=null && fluid.stored > 0;
	}

	@Override
	public boolean canFillFluidType(FluidStack fluidStack) {
		return fluid == null ? true : fluid.equalStack(fluidStack);
	}

	@Override
	public boolean canDrainFluidType(FluidStack fluidStack) {
		return fluid == null ? false : fluid.equalStack(fluidStack);
	}

}
