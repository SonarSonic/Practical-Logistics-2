package sonar.logistics.core.tiles.displays.info.types.fluids;

import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.*;
import sonar.logistics.api.core.tiles.displays.info.comparators.ComparableObject;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValueInfo;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IElementStorageHolder;
import sonar.logistics.core.tiles.displays.info.types.BaseInfo;

import java.util.List;

@ASMInfo(id = InfoNetworkFluid.id, modid = PL2Constants.MODID)
public class InfoNetworkFluid extends BaseInfo<InfoNetworkFluid> implements IJoinableInfo<InfoNetworkFluid>, INameableInfo<InfoNetworkFluid>, IComparableInfo<InfoNetworkFluid>, IMonitoredValueInfo<InfoNetworkFluid> {

	public static final String id = "fluid";
	public SyncNBTAbstract<StoredFluidStack> fluidStack = new SyncNBTAbstract<>(StoredFluidStack.class, 0);
	public final SyncTagType.INT networkID = (INT) new SyncTagType.INT(1).setDefault(-1);

	{
		syncList.addParts(fluidStack, networkID);
	}

	public InfoNetworkFluid() {}

	public InfoNetworkFluid(StoredFluidStack stack) {
		this.fluidStack.setObject(stack);
	}

	public InfoNetworkFluid(StoredFluidStack stack, int networkID) {
		this.fluidStack.setObject(stack);
		this.networkID.setObject(networkID);
	}

	@Override
	public boolean isIdenticalInfo(InfoNetworkFluid info) {
		return getStoredStack().equals(info.getStoredStack()) && networkID.getObject().equals(info.networkID.getObject());
	}

	@Override
	public boolean isMatchingInfo(InfoNetworkFluid info) {
		return getStoredStack().equalStack(info.getFluidStack());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof InfoNetworkFluid;
	}

	@Override
	public boolean canJoinInfo(InfoNetworkFluid info) {
		return isMatchingInfo(info);
	}

	@Override
	public IJoinableInfo joinInfo(InfoNetworkFluid info) {
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
	public InfoNetworkFluid copy() {
		return new InfoNetworkFluid(fluidStack.getObject().copy(), networkID.getObject());
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
		toAdd.add(new ElementNetworkFluid(uuid));
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
	public IMonitoredValue<InfoNetworkFluid> createMonitoredValue() {
		return new FluidCount(this);
	}

}
