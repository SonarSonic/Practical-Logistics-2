package sonar.logistics.base.requests.info;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;

import java.util.List;

public interface IInfoUUIDRequirementGui {

	void onInfoUUIDRequirementCompleted(List<InfoUUID> selected);
	
}
