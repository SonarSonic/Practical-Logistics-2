package sonar.logistics.base.requests.reference;

import sonar.logistics.core.tiles.displays.info.references.InfoReference;

import java.util.List;

public interface IInfoReferenceRequirementGui {

	void onReferenceRequirementCompleted(List<InfoReference> selected);
	
}
