package sonar.logistics.info.types;

import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.types.NetworkFluidElement;
import sonar.logistics.api.info.*;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.IMonitoredValueInfo;
import sonar.logistics.api.lists.values.FluidCount;
import sonar.logistics.api.tiles.signaller.ComparableObject;

import java.util.List;

@LogicInfoType(id = MonitoredFluidStack.id, modid = PL2Constants.MODID)
public class MonitoredFluidStack extends BaseInfo<MonitoredFluidStack> implements IJoinableInfo<MonitoredFluidStack>, INameableInfo<MonitoredFluidStack>, IComparableInfo<MonitoredFluidStack>, IMonitoredValueInfo<MonitoredFluidStack> {

	public static final String id = "fluid";
	public SyncNBTAbstract<StoredFluidStack> fluidStack = new SyncNBTAbstract<>(StoredFluidStack.class, 0);
	public final SyncTagType.INT networkID = (INT) new SyncTagType.INT(1).setDefault(-1);

	{
		syncList.addParts(fluidStack, networkID);
	}

	public MonitoredFluidStack() {}

	public MonitoredFluidStack(StoredFluidStack stack) {
		this.fluidStack.setObject(stack);
	}

	public MonitoredFluidStack(StoredFluidStack stack, int networkID) {
		this.fluidStack.setObject(stack);
		this.networkID.setObject(networkID);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredFluidStack info) {
		return getStoredStack().equals(info.getStoredStack()) && networkID.getObject().equals(info.networkID.getObject());
	}

	@Override
	public boolean isMatchingInfo(MonitoredFluidStack info) {
		return getStoredStack().equalStack(info.getFluidStack());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof MonitoredFluidStack;
	}

	@Override
	public boolean canJoinInfo(MonitoredFluidStack info) {
		return isMatchingInfo(info);
	}

	@Override
	public IJoinableInfo joinInfo(MonitoredFluidStack info) {
		fluidStack.getObject().add(info.fluidStack.getObject());
		return this;
	}

	@Override
	public boolean isValid() {
		return fluidStack.getObject() != null && fluidStack.getObject().fluid != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public MonitoredFluidStack copy() {
		return new MonitoredFluidStack(fluidStack.getObject().copy(), networkID.getObject());
	}

	@Override
	public String getClientIdentifier() {
		return (fluidStack.getObject() != null && fluidStack.getObject().fluid != null ? fluidStack.getObject().fluid.getLocalizedName() : "FLUIDSTACK");
	}

	@Override
	public String getClientObject() {
		return fluidStack.getObject() != null ? "" + FontHelper.formatFluidSize(fluidStack.getObject().stored) : "ERROR";
	}

	@Override
	public String getClientType() {
		return "fluid";
	}

	@Override
	public void createDefaultElements(List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		super.createDefaultElements(toAdd, h, uuid); //adds the text overlay too!
		toAdd.add(new NetworkFluidElement(uuid));
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		StoredFluidStack stack = fluidStack.getObject();
		objects.add(new ComparableObject(this, "Stored", stack.stored));
		objects.add(new ComparableObject(this, "Capacity", stack.capacity));
		return objects;
	}

	public String toString() {
		return fluidStack.getObject().toString();
	}

	public FluidStack getFluidStack() {
		return this.fluidStack.getObject().getFullStack();
	}

	public StoredFluidStack getStoredStack() {
		return this.fluidStack.getObject();
	}

	public long getStored() {
		return this.fluidStack.getObject().stored;
	}

	public int getNetworkSource() {
		return networkID.getObject();
	}

	@Override
	public IMonitoredValue<MonitoredFluidStack> createMonitoredValue() {
		return new FluidCount(this);
	}

}
