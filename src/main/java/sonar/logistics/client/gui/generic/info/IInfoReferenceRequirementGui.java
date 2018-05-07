package sonar.logistics.client.gui.generic.info;

import sonar.logistics.api.displays.references.InfoReference;

import java.util.List;

public interface IInfoReferenceRequirementGui {

	void onReferenceRequirementCompleted(List<InfoReference> selected);
	
}
