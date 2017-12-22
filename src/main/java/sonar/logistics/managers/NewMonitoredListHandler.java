package sonar.logistics.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import sonar.core.helpers.FunctionHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.utils.MonitoredValue;
import sonar.logistics.info.types.NewMonitoredList;
/*
public class NewMonitoredListHandler {

	public static final NewMonitoredListHandler handler = new NewMonitoredListHandler();

	public Map<Integer, HashMap<Object, MonitoredValue>> newMonitoredLists = Maps.newLinkedHashMap();
	public Map<Integer, NewMonitoredList> lists = Maps.newLinkedHashMap();

	public NewMonitoredListHandler() {}

	public static HashMap<Object, MonitoredValue> getMonitoredList(int hash) {
		return instance().newMonitoredLists.get(hash);
	}

	public static void finaliseList(NewMonitoredList list) {
		HashMap<Object, MonitoredValue> valueList = getOrCreateValueList(list.hashCode());
		valueList.values().forEach(value -> value.finalise());
	}

	public static void resetList(NewMonitoredList list) {
		HashMap<Object, MonitoredValue> valueList = getOrCreateValueList(list.hashCode());
		valueList.values().forEach(value -> value.reset());
	}

	public static void addList(NewMonitoredList list) {
		HashMap<Object, MonitoredValue> valueList = getOrCreateValueList(list.hashCode());
		for (Entry<Object, MonitoredValue> entry : valueList.entrySet()) {
			addStack(list, entry.getKey(), entry.getValue().getCurrent());
		}
	}

	public static void removeList(NewMonitoredList list) {
		HashMap<Object, MonitoredValue> valueList = getOrCreateValueList(list.hashCode());
		for (Entry<Object, MonitoredValue> entry : valueList.entrySet()) {
			removeStack(list, entry.getKey(), entry.getValue().getCurrent());
		}
	}

	public static MonitoredValue addStack(NewMonitoredList list, Object stack, long stored) {
		MonitoredValue value = getOrCreateValue(list, stack);
		value.add(stored);
		list.added(stack, stored);
		return value;
	}

	public static void removeStack(NewMonitoredList list, Object stack, long stored) {
		MonitoredValue value = getValue(list, stack);
		if (value != null) {
			value.remove(stored);
			list.removed(stack, stored);
		}
	}

	public static <T> MonitoredValue getOrCreateValue(NewMonitoredList list, T stack) {
		MonitoredValue value = getValue(list, stack);
		if (value == null) {
			HashMap<Object, MonitoredValue> valueList = getOrCreateValueList(list.hashCode());
			valueList.put(list.copy(stack), value = new MonitoredValue(0));
		}
		return value;
	}

	public static <T> MonitoredValue getValue(NewMonitoredList list, T stack) {
		HashMap<T, MonitoredValue> valueList = (HashMap<T, MonitoredValue>) getOrCreateValueList(list.hashCode());
		for (Entry<T, MonitoredValue> entry : valueList.entrySet()) {
			if (list.equal(stack, entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	public static HashMap<Object, MonitoredValue> getOrCreateValueList(int hash) {
		return instance().newMonitoredLists.computeIfAbsent(hash, FunctionHelper.HASH_MAP);
	}

	public static <T> List<HashMap<T, MonitoredValue>> forEachValue(NewMonitoredList<T> list, Consumer<? super Entry<T, MonitoredValue>> action) {
		HashMap<T, MonitoredValue> valueList = (HashMap<T, MonitoredValue>) getOrCreateValueList(list.hashCode());
		valueList.entrySet().forEach(action);
		return null;

	}

	public static void deleteList(int hash) {
		instance().newMonitoredLists.remove(hash);
		// send delete to client
	}

	public static NewMonitoredListHandler instance() {
		return handler; // make this not static
	}

	public static String HASH = "h";
	public static String VALUE_LIST = "v";

	/** need to finalise list 
	public static NBTTagCompound writeToNBT(NewMonitoredList list, NBTTagCompound tag, SyncType type) {
		NBTTagList tagList = new NBTTagList();
		forEachValue(list, E -> {
			if (E.getValue().shouldSave(type)) {
				tagList.appendTag(list.writeToNBT(new NBTTagCompound(), E.getKey(), E.getValue()));
			}
		});
		if (!tagList.hasNoTags()) {
			tag.setInteger(HASH, list.hashCode());
			tag.setTag(VALUE_LIST, tagList);
		}
		return tag;
	}

	public static NBTTagCompound readToNBT(NBTTagCompound tag, SyncType type) {
		int hash = tag.getInteger(HASH);
		NewMonitoredList list = instance().lists.get(hash);
		
		NBTTagList tagList = new NBTTagList();
		forEachValue(list, E -> {
			if (E.getValue().shouldSave(type)) {
				tagList.appendTag(list.writeToNBT(new NBTTagCompound(), E.getKey(), E.getValue()));
			}
		});
		if (!tagList.hasNoTags()) {
			tag.setInteger(HASH, list.hashCode());
			tag.setTag(VALUE_LIST, tagList);
		}
		return tag;
	}

}
*/