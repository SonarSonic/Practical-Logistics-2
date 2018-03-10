package sonar.logistics.client.gui.textedit;

import java.util.List;

import sonar.logistics.api.info.InfoUUID;

public interface IInfoUUIDRequirementGui {

	public void onRequirementCompleted(List<InfoUUID> selected);
	
}
