package sonar.logistics.api.displays;

import java.util.List;

import sonar.logistics.api.info.InfoUUID;

public interface IDisplayElementList extends IDisplayRenderable {

	void updateRender();
	
	double[] getMaxListScaling();
	
	int getDefaultColour();

	void doDefaultRender(IDisplayElement element);

	double[] createMaxScaling(IDisplayElement element);
	
	double[] createActualScaling(IDisplayElement element);

	void onInfoUUIDChanged(InfoUUID id);
	
	void onElementChanged(IDisplayElement element);

	List<IDisplayElement> getElements();
	
	
	
}
