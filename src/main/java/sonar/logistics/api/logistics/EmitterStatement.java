package sonar.logistics.api.logistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.network.sync.BaseSyncListPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagTypeList;
import sonar.core.network.sync.SyncUnidentifiedObject;
import sonar.logistics.Logistics;
import sonar.logistics.api.filters.BaseFilter;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.logic.comparators.ILogicComparator;

public class EmitterStatement<T> extends BaseSyncListPart implements ILogisticsStatement {

	public ILogicComparator<T> comparator;
	public SyncTagType.INT hashCode = new SyncTagType.INT(-1);
	public SyncTagType.INT comparatorID = new SyncTagType.INT(0);
	public SyncEnum<LogicOperator> operator = new SyncEnum(LogicOperator.values(), 1);

	/** if true, compare to bits of info, if false compare info to an object */
	public SyncEnum<InputTypes> useInfo = new SyncEnum(InputTypes.values(), 2);

	public SyncNBTAbstract<InfoUUID> uuid1 = new SyncNBTAbstract<InfoUUID>(InfoUUID.class, 3);
	public SyncNBTAbstract<InfoUUID> uuid2 = new SyncNBTAbstract<InfoUUID>(InfoUUID.class, 4);
	public SyncTagType.STRING key1 = new SyncTagType.STRING(5);
	public SyncTagType.STRING key2 = new SyncTagType.STRING(6);

	public SyncUnidentifiedObject obj = new SyncUnidentifiedObject(7);
	public SyncTagType.BOOLEAN wasTrue = new SyncTagType.BOOLEAN(9);
	// public SyncTagType.DOUBLE objNum = new SyncTagType.DOUBLE(7);
	// public SyncTagType.STRING objString = new SyncTagType.STRING(8);
	{
		syncList.addParts(hashCode, comparatorID, operator, useInfo, uuid1, uuid2, key1, key2, obj, wasTrue);
	}

	public EmitterStatement() {
		this.hashCode.setObject(UUID.randomUUID().hashCode());
	}

	public EmitterStatement(ILogicComparator<T> comparator) {
		this.comparator = comparator;
		this.comparatorID.setObject(Logistics.comparatorRegistry.getObjectID(comparator.getName()));
		this.operator.setObject(comparator.getValidOperators().get(0));
		this.hashCode.setObject(UUID.randomUUID().hashCode());
	}

	public void addRequiredUUIDs(List<InfoUUID> uuids) {
		if (uuid1.getObject() != null && !uuids.contains(uuid1.getObject())) {
			uuids.add(uuid1.getObject());
		}
		if (useInfo.getObject().usesInfo()) {
			if (uuid2.getObject() != null && !uuids.contains(uuid2.getObject())) {
				uuids.add(uuid2.getObject());
			}
		}
	}

	public ILogicComparator<T> getComparator() {
		if (comparator == null) {
			comparator = Logistics.comparatorRegistry.getRegisteredObject(comparatorID.getObject());
		}
		return comparator;
	}

	public Object getObject(Map<InfoUUID, IMonitorInfo> info, int pos) {
		if (pos == 0) {
			return getObject(info, uuid1.getObject(), key1.getObject());
		} else if (useInfo.getObject().usesInfo()) {
			return getObject(info, uuid2.getObject(), key2.getObject());
		} else if (obj.get() != null && getComparator().isValidObject(obj.get()))
			return obj.get();
		return null;

	}

	public Object getObject(Map<InfoUUID, IMonitorInfo> info, InfoUUID id, String key) {
		if (id != null && key != null) {
			IMonitorInfo info1 = info.get(id);
			if (info1 != null && info1 instanceof IComparableInfo) {
				IComparableInfo provider1 = (IComparableInfo) info1;
				ComparableObject obj = ComparableObject.getComparableObject(provider1.getComparableObjects(new ArrayList()), key);
				if (getComparator().isValidObject(obj.object)) {
					return obj.object;
				}

			}
		}
		return null;
	}

	@Override
	public LogicState isMatching(Map<InfoUUID, IMonitorInfo> info) {
		T num1 = (T) getObject(info, 0);
		T num2 = (T) getObject(info, 1);
		if (num1 != null && num2 != null) {
			return getComparator().getLogicState(operator.getObject(), num1, num2);
		}
		return LogicState.FALSE;
	}

	@Override
	public LogicOperator getOperator() {
		return operator.getObject();
	}

	@Override
	public ArrayList<LogicOperator> validOperators() {
		return getComparator().getValidOperators();
	}

	public int hashCode() {
		return this.hashCode.getObject();
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof EmitterStatement) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	public InputTypes getInputType() {
		return useInfo.getObject();
	}

	public void incrementInputType() {
		useInfo.incrementEnum();
	}

	public void incrementOperator() {
		LogicOperator current = operator.getObject();
		ArrayList<LogicOperator> valid = validOperators();
		int pos = -1;
		int currentPos = 0;
		for (LogicOperator op : validOperators()) {
			if (op == current) {
				pos = currentPos;
				break;
			}
			currentPos++;
		}
		if (pos == -1) {
			operator.setObject(valid.get(0));
		}
		int ordinal = pos + 1;
		if (ordinal < valid.size()) {
			operator.setObject(valid.get(ordinal));
		} else {
			operator.setObject(valid.get(0));
		}
	}
}
