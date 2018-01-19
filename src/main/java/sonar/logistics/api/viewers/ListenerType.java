package sonar.logistics.api.viewers;

import java.util.List;

import sonar.core.helpers.SonarHelper;

public enum ListenerType {
	LISTENER, //previously info
	
	CHANNEL_LISTENER, //previous channels
	
	TEMP_LISTENER, //previously temporary
	
	NEW_LISTENER; //previous fullinfo
	
	public static final List<ListenerType> ALL = SonarHelper.convertArray(ListenerType.values());
}
