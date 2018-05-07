package sonar.logistics.client.gui.generic.info;

import sonar.logistics.api.info.InfoUUID;

import java.util.List;

public interface IInfoUUIDRequirementGui {

	void onInfoUUIDRequirementCompleted(List<InfoUUID> selected);
	
}
