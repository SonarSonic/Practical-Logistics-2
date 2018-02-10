package sonar.logistics.api.tiles.signaller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import sonar.core.network.sync.BaseSyncListPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUnidentifiedObject;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.ComparatorHelper;
import sonar.logistics.info.comparators.ILogicComparator;
import sonar.logistics.packets.sync.SyncInfoUUID;

public class EmitterStatement<T> extends BaseSyncListPart implements ILogisticsStatement {

	private ILogicComparator<T> comparator;
	public SyncTagType.INT hashCode = new SyncTagType.INT(-1);
	public SyncTagType.STRING comparatorID = new SyncTagType.STRING(0);
	public SyncEnum<LogicOperator> operator = new SyncEnum(LogicOperator.values(), 1);

	/** if true, compare to bits of info, if false compare info to an object */
	public SyncEnum<InputTypes> useInfo = new SyncEnum(InputTypes.values(), 2);

	public SyncNBTAbstract<InfoUUID> uuid1 = new SyncInfoUUID(3).setObject(InfoUUID.newInvalid());
	public SyncNBTAbstract<InfoUUID> uuid2 = new SyncInfoUUID(4).setObject(InfoUUID.newInvalid());
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
		//this.comparator = comparator;
		this.comparatorID.setObject(comparator.getName());
		this.operator.setObject(comparator.getValidOperators().get(0));
		this.hashCode.setObject(UUID.randomUUID().hashCode());
	}

	public void addRequiredUUIDs(List<InfoUUID> uuids) {
		if (InfoUUID.valid(uuid1.getObject()) && !uuids.contains(uuid1.getObject())) {
			uuids.add(uuid1.getObject());
		}
		if (useInfo.getObject().usesInfo()) {
			if (InfoUUID.valid(uuid2.getObject()) && !uuids.contains(uuid2.getObject())) {
				uuids.add(uuid2.getObject());
			}
		}
	}

	public ILogicComparator<T> getComparator() {
		//if (comparator == null) {
		//	comparator = ComparatorHelper.getComparator(comparatorID.getObject());
		//}
		return ComparatorHelper.getComparator(comparatorID.getObject());
	}

	public Object getObject(Map<InfoUUID, IInfo> info, int pos) {
		if (pos == 0) {
			return getObject(info, uuid1.getObject(), key1.getObject());
		} else if (useInfo.getObject().usesInfo()) {
			return getObject(info, uuid2.getObject(), key2.getObject());
		} else if (obj.get() != null && getComparator().isValidObject(obj.get()))
			return obj.get();
		return null;

	}

	public Object getObject(Map<InfoUUID, IInfo> info, InfoUUID id, String key) {
		if (InfoUUID.valid(id) && key != null) {
			IInfo info1 = info.get(id);
			if (info1 != null && info1 instanceof IComparableInfo) {
				IComparableInfo provider1 = (IComparableInfo) info1;
				ComparableObject obj = ComparableObject.getComparableObject(provider1.getComparableObjects(Lists.newArrayList()), key);
				if (getComparator() != null && getComparator().isValidObject(obj.object)) {
					return obj.object;
				}

			}
		}
		return null;
	}

	@Override
	public LogicState isMatching(Map<InfoUUID, IInfo> info) {
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
	public List<LogicOperator> validOperators() {
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
		List<LogicOperator> valid = validOperators();
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
