package sonar.logistics.api.displays;

import sonar.logistics.api.displays.elements.HeightAlignment;
import sonar.logistics.api.displays.elements.WidthAlignment;

public interface IDisplayElement extends IDisplayRenderable {

	IDisplayElementList getList();

	default void render() {
		getList().doDefaultRender(this);
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
