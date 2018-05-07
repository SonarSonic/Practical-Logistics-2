package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.references.InfoReference;

import java.util.List;

public interface IInfoReferenceRequirement {
	
	int getReferencesRequired();	
	
	List<InfoReference> getSelectedReferences();
	
	/**sends the selected info to the server*/
	void onGuiClosed(List<InfoReference> selected);	
	
	//void doInfoRequirementPacket(DisplayGSI gsi, EntityPlayer player, List<InfoReference> require, int requirementRef);
}
