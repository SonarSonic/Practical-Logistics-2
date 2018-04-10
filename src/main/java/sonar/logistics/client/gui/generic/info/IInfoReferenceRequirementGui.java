package sonar.logistics.client.gui.generic.info;

import java.util.List;

import sonar.logistics.api.displays.references.InfoReference;

public interface IInfoReferenceRequirementGui {

	public void onReferenceRequirementCompleted(List<InfoReference> selected);
	
}
