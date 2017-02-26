package sonar.logistics.connections.monitoring;

import java.util.ArrayList;

import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.utils.BlockCoords;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IJoinableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.logistics.ComparableObject;
import sonar.logistics.info.types.BaseInfo;
import sonar.logistics.network.SyncMonitoredType;

@LogicInfoType(id = MonitoredEnergyStack.id, modid = Logistics.MODID)
public class MonitoredEnergyStack extends BaseInfo<MonitoredEnergyStack> implements IJoinableInfo<MonitoredEnergyStack>, INameableInfo<MonitoredEnergyStack>, IComparableInfo<MonitoredEnergyStack> {

	public static final String id = "energy";
	public static LogicMonitorHandler<MonitoredEnergyStack> handler = LogicMonitorHandler.instance(EnergyMonitorHandler.id);
	public SyncNBTAbstract<StoredEnergyStack> energyStack = new SyncNBTAbstract<StoredEnergyStack>(StoredEnergyStack.class, 0);
	public SyncMonitoredType<MonitoredBlockCoords> coords = new SyncMonitoredType<MonitoredBlockCoords>(1);

	{
		syncList.addParts(energyStack, coords);
	}

	public MonitoredEnergyStack() {
	}

	public MonitoredEnergyStack(StoredEnergyStack stack, MonitoredBlockCoords coords) {
		this.energyStack.setObject(stack);
		this.coords.setInfo(coords);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredEnergyStack info) {
		return energyStack.getObject().equals(info.energyStack.getObject()) && coords.getMonitoredInfo().isIdenticalInfo(info.coords.getMonitoredInfo());
	}

	@Override
	public boolean isMatchingInfo(MonitoredEnergyStack info) {
		return (energyStack.getObject().energyType==null || energyStack.getObject().energyType.equals(info.energyStack.getObject().energyType)) && coords.getMonitoredInfo().isMatchingInfo(info.coords.getMonitoredInfo());
	}

	@Override
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof MonitoredEnergyStack;
	}

	@Override
	public LogicMonitorHandler<MonitoredEnergyStack> getHandler() {
		return handler;
	}

	@Override
	public boolean canJoinInfo(MonitoredEnergyStack info) {
		return false;// isMatchingInfo(info);
	}

	@Override
	public IJoinableInfo joinInfo(MonitoredEnergyStack info) {
		energyStack.getObject().add(info.energyStack.getObject());
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
		return new MonitoredEnergyStack(energyStack.getObject().copy(), coords.getMonitoredInfo().copy());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {

	}

	@Override
	public String getClientIdentifier() {
		return "Energy: " + (energyStack.getObject() != null && energyStack.getObject().energyType != null ? energyStack.getObject().energyType.getName() : "ENERGYSTACK");
	}

	@Override
	public String getClientObject() {
		return energyStack.getObject() != null ? "" + energyStack.getObject().stored : "ERROR";
	}

	@Override
	public String getClientType() {
		return "energy";
	}

	@Override
	public ArrayList<ComparableObject> getComparableObjects(ArrayList<ComparableObject> objects) {
		BlockCoords blockCoords = coords.getMonitoredInfo().syncCoords.getCoords();
		StoredEnergyStack stack = energyStack.getObject();
		objects.add(new ComparableObject(this, "x", blockCoords.getX()));
		objects.add(new ComparableObject(this, "y", blockCoords.getY()));
		objects.add(new ComparableObject(this, "z", blockCoords.getZ()));
		objects.add(new ComparableObject(this, "input", stack.input));
		objects.add(new ComparableObject(this, "output", stack.output));
		objects.add(new ComparableObject(this, "stored", stack.stored));
		objects.add(new ComparableObject(this, "capacity", stack.capacity));
		objects.add(new ComparableObject(this, "types", stack.energyType.toString()));
		return null;
	}

}
