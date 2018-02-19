package sonar.logistics.info.types;

import java.util.List;

import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IJoinableInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.packets.sync.SyncMonitoredType;

@LogicInfoType(id = MonitoredEnergyStack.id, modid = PL2Constants.MODID)
public class MonitoredEnergyStack extends BaseInfo<MonitoredEnergyStack> implements IJoinableInfo<MonitoredEnergyStack>, INameableInfo<MonitoredEnergyStack>, IComparableInfo<MonitoredEnergyStack> {

	public static final String id = "energy";
	private SyncNBTAbstract<StoredEnergyStack> energyStack = new SyncNBTAbstract<StoredEnergyStack>(StoredEnergyStack.class, 0);
	private SyncMonitoredType<MonitoredBlockCoords> coords = new SyncMonitoredType<MonitoredBlockCoords>(1);
	private final SyncNBTAbstract<StoredItemStack> dropStack = new SyncNBTAbstract<StoredItemStack>(StoredItemStack.class, 3);

	{
		syncList.addParts(energyStack, coords, dropStack);
	}

	public MonitoredEnergyStack() {
	}

	public MonitoredEnergyStack(StoredEnergyStack stack, MonitoredBlockCoords coords, StoredItemStack dropstack) {
		this.energyStack.setObject(stack);
		this.coords.setInfo(coords);
		this.dropStack.setObject(dropstack);

	}

	public MonitoredEnergyStack(StoredEnergyStack stack, MonitoredBlockCoords coords) {
		this.energyStack.setObject(stack);
		this.coords.setInfo(coords);
		this.dropStack.setObject(new StoredItemStack(LogisticsHelper.getCoordItem(coords.getCoords())));
	}

	@Override
	public boolean isIdenticalInfo(MonitoredEnergyStack info) {
		return energyStack.getObject().equals(info.energyStack.getObject()) && coords.getMonitoredInfo().isIdenticalInfo(info.coords.getMonitoredInfo());
	}

	@Override
	public boolean isMatchingInfo(MonitoredEnergyStack info) {

		return coords.getMonitoredInfo().isMatchingInfo(info.coords.getMonitoredInfo()); //&& info.dropStack.getObject() == null || dropStack.getObject()==null) ? true : dropStack.getObject().equals(info.dropStack.getObject());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof MonitoredEnergyStack;
	}

	@Override
	public boolean canJoinInfo(MonitoredEnergyStack info) {
		return false;// isMatchingInfo(info);
	}

	@Override
	public IJoinableInfo joinInfo(MonitoredEnergyStack info) {
		energyStack.getObject().add(info.energyStack.getObject());
		energyStack.markChanged();
		return this;
	}

	@Override
	public boolean isValid() {
		return energyStack.getObject() != null && energyStack.getObject().energyType != null && coords.getMonitoredInfo() != null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public MonitoredEnergyStack copy() {
		return new MonitoredEnergyStack(energyStack.getObject().copy(), coords.getMonitoredInfo().copy(), dropStack.getObject().copy());
	}


	@Override
	public String getClientIdentifier() {
		return (energyStack.getObject() != null && energyStack.getObject().energyType != null ? energyStack.getObject().energyType.getName() : "ENERGYSTACK");
	}

	@Override
	public String getClientObject() {
		return energyStack.getObject() != null ? "" + FontHelper.formatStorage(energyStack.obj.energyType, energyStack.getObject().stored) + " / " + FontHelper.formatStorage(energyStack.obj.energyType, energyStack.getObject().capacity) : "ERROR";
	}

	@Override
	public String getClientType() {
		return "energy";
	}
	
	public MonitoredBlockCoords getMonitoredCoords(){
		return coords.getMonitoredInfo();
	}
	
	public StoredEnergyStack getEnergyStack(){
		return energyStack.getObject();
	}
	
	public StoredItemStack getDropStack(){
		return dropStack.getObject();
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		BlockCoords blockCoords = getMonitoredCoords().getCoords();
		StoredEnergyStack stack = getEnergyStack();
		objects.add(new ComparableObject(this, "x", blockCoords.getX()));
		objects.add(new ComparableObject(this, "y", blockCoords.getY()));
		objects.add(new ComparableObject(this, "z", blockCoords.getZ()));
		objects.add(new ComparableObject(this, "input", stack.input));
		objects.add(new ComparableObject(this, "output", stack.output));
		objects.add(new ComparableObject(this, "stored", stack.stored));
		objects.add(new ComparableObject(this, "capacity", stack.capacity));
		objects.add(new ComparableObject(this, "types", stack.energyType.toString()));
		return objects;
	}

}
