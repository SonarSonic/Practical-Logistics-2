package sonar.logistics.core.tiles.displays.info.elements.base;

import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;

import java.util.List;

public interface IInfoReferenceElement extends IDisplayElement {

	/**all the {@link InfoUUID} which are required to render this DisplayElement,
	 * this information is used to know which {@link IInfo} should be synced with the client*/
	List<InfoUUID> getInfoReferences();
	
	/**called when info included in {@link #getInfoReferences} is changed*/
	default void onInfoReferenceChanged(InfoUUID uuid, IInfo info) {}

	/**called when an {@link AbstractChangeableList} whose {@link InfoUUID} included in {@link #getInfoReferences} is changed*/
	default void onChangeableListChanged(InfoUUID uuid, AbstractChangeableList list) {}
		
	default void validate(DisplayGSI gsi){}
	
	default void invalidate(DisplayGSI gsi){}
}
