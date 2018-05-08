package sonar.logistics.core.tiles.displays.info.elements.base;

import net.minecraft.util.Tuple;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.gsi.storage.ElementStorage;

public interface IElementStorageHolder {

	ElementStorage getElements();
	
	DisplayElementContainer getContainer();
	
	double[] getAlignmentTranslation();
	
	double[] getAlignmentTranslation(IDisplayElement e);
	
	void startElementRender(IDisplayElement e);
	
	void endElementRender(IDisplayElement e);
	
	Tuple<IDisplayElement, double[]> getClickBoxes(double x, double y);

	void onElementAdded(IDisplayElement element);

	void onElementRemoved(IDisplayElement element);
	
	double[] getMaxScaling();	
	
	double[] getActualScaling();

	double[] createMaxScaling(IDisplayElement element);

	double[] createActualScaling(IDisplayElement element);
	
	void updateActualScaling(); 
	
}
