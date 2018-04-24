package sonar.logistics.client.gui.generic.info;

import java.util.List;

import sonar.logistics.api.displays.references.InfoReference;

public interface IInfoReferenceRequirementGui {

	void onReferenceRequirementCompleted(List<InfoReference> selected);
	
}
