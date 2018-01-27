package sonar.logistics.client.gsi;

import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;

public interface IGSIListViewer {

	public void setCachedList(AbstractChangeableList list, InfoUUID id);
}
