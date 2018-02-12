package sonar.logistics.api.displays;

import java.util.List;

import com.google.common.collect.Lists;

import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.HeightAlignment;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.WidthAlignment;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;

public interface IDisplayElement extends IDisplayRenderable, INBTSyncable {

	IElementStorageHolder setHolder(IElementStorageHolder c);
	
	IElementStorageHolder getHolder();

	String getRegisteredName();
	
	List<InfoUUID> getInfoReferences();
	
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
