package sonar.logistics.client.gui.generic.info;

import java.util.List;

import sonar.logistics.api.info.InfoUUID;

public interface IInfoUUIDRequirementGui {

	public void onRequirementCompleted(List<InfoUUID> selected);
	
}
