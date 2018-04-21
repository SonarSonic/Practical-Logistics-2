package sonar.logistics.api.displays.elements;

import java.util.List;

import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;

public interface IInfoReferenceElement extends IDisplayElement {

	/**all the {@link InfoUUID} which are required to render this DisplayElement,
	 * this information is used to know which {@link IInfo} should be synced with the client*/
	List<InfoUUID> getInfoReferences();
	
	/**called when info included in {@link #getInfoReferences} is changed*/
	default void onInfoReferenceChanged(InfoUUID uuid, IInfo info) {}

	/**called when an {@link AbstractChangeableList} whose {@link InfoUUID} included in {@link #getInfoReferences} is changed*/
	default void onChangeableListChanged(InfoUUID uuid, AbstractChangeableList list) {}
		
	default void validate(DisplayGSI gsi){
		//getInfoReferences().forEach(holder -> holder.validate(gsi, this));
	}
	
	default void invalidate(DisplayGSI gsi){
		//getInfoReferences().forEach(holder -> holder.invalidate(gsi, this));
	}
}
