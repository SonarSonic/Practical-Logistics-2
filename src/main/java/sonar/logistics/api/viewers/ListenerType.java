package sonar.logistics.api.viewers;

import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.SonarHelper;
import sonar.logistics.api.utils.Result;

import java.util.List;

public enum ListenerType {

	NEW_GUI_LISTENER(UpdateType.GUI, SyncType.SAVE, 10), // should update all entire monitored info list, and send it

	TEMPORARY_LISTENER(UpdateType.GUI, SyncType.SAVE, 1), // should update everything shown in gui?

	OLD_GUI_LISTENER(UpdateType.GUI, SyncType.DEFAULT_SYNC, 0), // should update monitored list, but only send changes

	NEW_DISPLAY_LISTENER(UpdateType.DISPLAY, SyncType.SAVE, 10), // should update all info/monitored list on display, and send it

	OLD_DISPLAY_LISTENER(UpdateType.DISPLAY, SyncType.DEFAULT_SYNC, 0), // should update all info/monitored list on display, but only send changes

	NEW_CHANNEL_LISTENER(UpdateType.CHANNEL, SyncType.SAVE, 10), // should update channels list, and send it

	OLD_CHANNEL_LISTENER(UpdateType.CHANNEL, SyncType.DEFAULT_SYNC, 0), // should update channel list, and send changes.

	NONE(UpdateType.NONE, SyncType.NONE, 0); // nothing needs updating

	public static final List<ListenerType> ALL = SonarHelper.convertArray(ListenerType.values());

	private UpdateType updateType;
	private SyncType syncType;
	public int order;

	ListenerType(UpdateType updateType, SyncType syncType, int order) {
		this.updateType = updateType;
		this.syncType = syncType;
	}

	public static Result canReplace(ListenerType toReplace, ListenerType type) {
		if (toReplace.getUpdateType() != type.getUpdateType()) {
			return Result.PASS;
		}
		return toReplace.order < type.order ? Result.SUCCESS : Result.FAIL;
	}

	public enum UpdateType {
		GUI, DISPLAY, CHANNEL, NONE
    }
	
	public boolean shouldForceUpdate() {
		return getSyncType() == SyncType.SAVE;
	}
	
	public boolean shouldSyncUpdate() {
		return getSyncType().isType(SyncType.SAVE, SyncType.DEFAULT_SYNC);
	}

	public boolean shouldForceUpdate(UpdateType type) {
		return canUpdateType(type) && shouldForceUpdate();
	}

	public boolean shouldSyncUpdate(UpdateType type) {
		return canUpdateType(type) && shouldSyncUpdate();
	}

	public boolean canUpdateType(UpdateType type) {
		return this.updateType == type;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public SyncType getSyncType() {
		return syncType;
	}

	public boolean isType(ListenerType... types) {
		for (ListenerType type : types) {
			if (type == this) {
				return true;
			}
		}
		return false;
	}
}
