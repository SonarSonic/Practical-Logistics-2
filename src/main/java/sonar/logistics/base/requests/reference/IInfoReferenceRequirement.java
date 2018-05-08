package sonar.logistics.base.requests.reference;

import sonar.logistics.core.tiles.displays.info.references.InfoReference;

import java.util.List;

public interface IInfoReferenceRequirement {
	
	int getReferencesRequired();	
	
	List<InfoReference> getSelectedReferences();
	
	/**sends the selected info to the server*/
	void onGuiClosed(List<InfoReference> selected);
}
