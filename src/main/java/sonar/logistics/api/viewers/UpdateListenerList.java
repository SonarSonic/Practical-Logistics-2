package sonar.logistics.api.viewers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sonar.logistics.api.viewers.ListenerType.UpdateType;

public class UpdateListenerList {

	private Map<UpdateType, ListenerType> types = new HashMap<>(); // sub type with the current ListenerType
	private Map<UpdateType, Boolean> canAdd = new HashMap<>(); // sub type with the current ListenerType
	private UpdateType[] validTypes;

	public UpdateListenerList() {
		validTypes = UpdateType.values();
	}

	public UpdateListenerList(UpdateType[] types) {
		validTypes = types;
	}

	public void reset() {
		types.clear();
		canAdd.clear();
	}

	public boolean isValid(UpdateType type) {
		for (UpdateType t : validTypes) {
			if (t == type) {
				return true;
			}
		}
		return false;
	}

	public void add(ListenerType type) {
		if (canAdd(type.getUpdateType())) {
			ListenerType current = types.get(type.getUpdateType());
			if (current == null || ListenerType.canReplace(current, type).getBoolean()) {
				types.put(type.getUpdateType(), type);
				if (type.order == 10) {
					setMax(type.getUpdateType());
				}
			}
		}
	}

	public boolean canAdd(UpdateType updateType) {
		if (!isValid(updateType)) {
			return false;
		}
		Boolean bool = canAdd.get(updateType);
		return bool == null ? true : bool;
	}

	public void setMax(UpdateType updateType) {
		canAdd.put(updateType, false);
	}

	public boolean canForceUpdate(UpdateType ...updateTypes) {
		for(UpdateType updateType :updateTypes){
			ListenerType type = types.get(updateType);
			if(type!=null && type.shouldForceUpdate()){
				return true;
			}
		}		
		return false;
	}

	public boolean canSyncUpdate(UpdateType ...updateTypes) {
		for(UpdateType updateType :updateTypes){
			ListenerType type = types.get(updateType);
			if(type!=null && type.shouldSyncUpdate()){
				return true;
			}
		}		
		return false;
	}

	public Collection<ListenerType> getUpdates() {
		return types.values();
	}

	public boolean canAccept() {
		for (UpdateType type : UpdateType.values()) {
			if (canAdd(type)) {
				return true;
			}
		}
		return false;
	}

}
