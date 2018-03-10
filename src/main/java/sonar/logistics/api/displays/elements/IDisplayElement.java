package sonar.logistics.api.displays.elements;

import java.util.List;

import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;

public interface IDisplayElement extends IDisplayRenderable, INBTSyncable {

	IElementStorageHolder setHolder(IElementStorageHolder c);
	
	IElementStorageHolder getHolder();
	
	int getElementIdentity();
	
	default DisplayGSI getGSI(){
		return getHolder().getContainer().getGSI();
	}

	String getRegisteredName();
	
	List<InfoUUID> getInfoReferences();
	
	default void onInfoReferenceChanged(InfoUUID uuid, IInfo info){}
	
	default void render() {	
		FontHelper.text(getRepresentiveString(), 0, 0, getHolder().getContainer().getDefaultColour());
	}
	
	default void updateRender(){
		//CAN BE USED TO MARK THE ELEMENT AS CHANGED
	}
	
	void onElementChanged();

	double getPercentageFill();
	
	double setPercentageFill(double fill);
	
	String getRepresentiveString();

	/**the width/height in pixels, returns 0,0 if this element fills the entire container*/
	
	default ElementFillType getFillType(){
		return ElementFillType.CUSTOM_SIZE;
	}
	
	int[] getUnscaledWidthHeight();
	
	WidthAlignment getWidthAlignment();

	WidthAlignment setWidthAlignment(WidthAlignment align);
	
	HeightAlignment getHeightAlignment();

	HeightAlignment setHeightAlignment(HeightAlignment align);
		
	double[] setMaxScaling(double[] scaling);

	double[] getMaxScaling();	
	
	double[] setActualScaling(double[] scaling);

	double[] getActualScaling();

}