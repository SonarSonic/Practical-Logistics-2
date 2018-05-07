package sonar.logistics.networking.info;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.lists.EnumListChange;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;

import java.util.List;

public class InfoHelper {

	public static boolean isMatchingInfo(IInfo info, IInfo info2) {
		return info.isMatchingType(info2) && info.isMatchingInfo(info2);
	}

	public static boolean isIdenticalInfo(IInfo info, IInfo info2) {
		return isMatchingInfo(info, info2) && info.isIdenticalInfo(info2);
	}

	public static final String DELETE = "del";
	public static final String SAVED = "saved";
	public static final String REMOVED = "rem";
	public static final String SYNCED = "spe";


	public static <T extends IInfo> NBTTagCompound writeMonitoredList(NBTTagCompound tag, AbstractChangeableList<T> stacks, SyncType type) {
		if (type.isType(SyncType.DEFAULT_SYNC)) {
			List<IMonitoredValue<T>> values = stacks.getList();
			if ((values == null || values.isEmpty())) {
				tag.setBoolean(DELETE, true);
				return tag;
			}
			NBTTagList list = new NBTTagList();
			for (IMonitoredValue<T> value : values) {
				EnumListChange change = value.getChange();
				if (change.shouldUpdate()) {
					NBTTagCompound compound = new NBTTagCompound();
					IInfo info = value.getSaveableInfo();
					list.appendTag(InfoHelper.writeInfoToNBT(compound, info, SyncType.SAVE));// change to sync so info can do it's update
					if (value.shouldDelete(change))
						compound.setBoolean(REMOVED, true);
				}
			}
			if (list.tagCount() != 0) {
				tag.setTag(SYNCED, list);
			}
		} else if (type.isType(SyncType.SAVE)) {
			NBTTagList list = new NBTTagList();
			List<IMonitoredValue<T>> values = stacks.getList();
			values.forEach(value -> list.appendTag(InfoHelper.writeInfoToNBT(new NBTTagCompound(), value.getSaveableInfo(), SyncType.SAVE)));
			tag.setTag(SAVED, list);
		}
		return tag;
	}

	// FIXME - to use updateWriting for some of the tags, like ILogicInfo
	/* public static <T extends IInfo> NBTTagCompound writeMonitoredList(NBTTagCompound tag, boolean lastWasNull, MonitoredList<T> stacks, SyncType type) { if (type.isType(SyncType.DEFAULT_SYNC)) { stacks.sizing.writeData(tag, SyncType.SAVE); NBTTagList list = new NBTTagList(); stacks.forEach(info -> { if (info != null && info.isValid()) { list.appendTag(InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE)); } }); if (list.tagCount() != 0) { tag.setTag(SYNC, list); return tag; } else { // if (!lastWasNull) tag.setBoolean(DELETE, true); return tag; } } else if (type.isType(SyncType.SPECIAL)) { if (!stacks.changed.isEmpty() || !stacks.removed.isEmpty()) { stacks.sizing.writeData(tag, SyncType.DEFAULT_SYNC); if ((stacks == null || stacks.isEmpty())) { if (!lastWasNull) tag.setBoolean(DELETE, true); return tag; } NBTTagList list = new NBTTagList(); for (int listType = 0; listType < 2; listType++) { List<T> stackList = listType == 0 ? stacks.changed : stacks.removed; for (int i = 0; i < stackList.size(); i++) { T info = stackList.get(i); if (info != null && info.isValid()) { NBTTagCompound compound = new NBTTagCompound(); compound.setBoolean(REMOVED, listType == 1); list.appendTag(InfoHelper.writeInfoToNBT(compound, info, SyncType.SAVE)); } } } if (list.tagCount() != 0) { tag.setTag(SPECIAL, list); } } } return tag; } */
	public static <L extends AbstractChangeableList> L readMonitoredList(NBTTagCompound tag, L stacks, SyncType type) {
		if (tag.hasKey(DELETE)) {
			stacks.values.clear();
			return stacks;
		}
		if (type.isType(SyncType.SAVE)) {
			if (!tag.hasKey(SAVED)) {
				return stacks;
			}
			NBTTagList list = tag.getTagList(SAVED, 10);
			stacks.values.clear();
			for (int i = 0; i < list.tagCount(); i++) {
				stacks.add(InfoHelper.readInfoFromNBT(list.getCompoundTagAt(i)));
			}
		} else if (type.isType(SyncType.DEFAULT_SYNC)) {
			if (!tag.hasKey(SYNCED)) {
				return stacks;
			}
			NBTTagList list = tag.getTagList(SYNCED, 10);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound infoTag = list.getCompoundTagAt(i);
				boolean removed = infoTag.getBoolean(REMOVED);
				IInfo stack = InfoHelper.readInfoFromNBT(infoTag);
				IMonitoredValue value = stacks.find(stack);
				if (value == null) {
					if (!removed) {
						stacks.add(stack);
					}
				} else if (removed) {
					stacks.values.remove(value);
				} else {
					value.reset(stack);
				}

			}
		}
		return stacks;
	}

		public static boolean hasInfoChanged(IInfo info, IInfo newInfo) {
		if (info == null && newInfo == null) {
			return false;
		} else if (info == null && newInfo != null || newInfo == null) {
			return true;
		}
		return !info.isMatchingType(newInfo) || !info.isMatchingInfo(newInfo) || !info.isIdenticalInfo(newInfo);
	}

	public static int getName(String name) {
		return PL2ASMLoader.infoIds.get(name);
	}

	public static Class<? extends IInfo> getInfoType(int id) {
		return PL2ASMLoader.infoClasses.get(PL2ASMLoader.infoNames.get(id));
	}

	public static NBTTagCompound writeInfoToNBT(NBTTagCompound tag, IInfo info, SyncType type) {
		tag.setInteger("iiD", PL2ASMLoader.infoIds.get(info.getID()));
		info.writeData(tag, type);
		return tag;
	}

	public static IInfo readInfoFromNBT(NBTTagCompound tag) {
		return loadInfo(tag.getInteger("iiD"), tag);
	}

	public static IInfo loadInfo(int id, NBTTagCompound tag) {
		return NBTHelper.instanceNBTSyncable(getInfoType(id), tag);
	}

	public static INodeFilter readFilterFromNBT(NBTTagCompound tag) {
		return NBTHelper.instanceNBTSyncable(PL2ASMLoader.filterClasses.get(tag.getString("id")), tag);
	}

	public static NBTTagCompound writeFilterToNBT(NBTTagCompound tag, INodeFilter filter, SyncType type) {
		tag.setString("id", filter.getNodeID());
		filter.writeData(tag, type);
		return tag;
	}

}
