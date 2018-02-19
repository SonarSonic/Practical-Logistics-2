package sonar.logistics.api.displays.elements;

import net.minecraft.util.Tuple;
import sonar.logistics.api.displays.IDisplayElement;

public class ChangeableInfoTypeHolder implements IElementStorageHolder {

	@Override
	public ElementStorage getElements() {
		return null;
	}

	@Override
	public DisplayElementContainer getContainer() {
		return null;
	}

	@Override
	public double[] getAlignmentTranslation() {
		return null;
	}

	@Override
	public double[] getAlignmentTranslation(IDisplayElement e) {
		return null;
	}

	@Override
	public void startElementRender(IDisplayElement e) {
		
	}

	@Override
	public void endElementRender(IDisplayElement e) {
		
	}

	@Override
	public Tuple<IDisplayElement, double[]> getClickBoxes(double x, double y) {
		return null;
	}

	@Override
	public void onElementAdded(IDisplayElement element) {
		
	}

	@Override
	public void onElementRemoved(IDisplayElement element) {
		
	}

	@Override
	public double[] getMaxScaling() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getActualScaling() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] createMaxScaling(IDisplayElement element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] createActualScaling(IDisplayElement element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateActualScaling() {
		// TODO Auto-generated method stub
		
	}

}
