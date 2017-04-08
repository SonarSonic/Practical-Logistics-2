package sonar.logistics.api.viewers;

import java.util.List;

import sonar.core.helpers.SonarHelper;

public enum ListenerType {
	INFO, CHANNEL, TEMPORARY, FULL_INFO;
	
	public static final List<ListenerType> ALL = SonarHelper.convertArray(ListenerType.values());
}
