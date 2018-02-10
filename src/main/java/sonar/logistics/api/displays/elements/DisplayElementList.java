package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.IDisplayElementList;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;

public class DisplayElementList implements IDisplayElementList {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	private List<IDisplayElement> elements;
	protected double[] maxListScaling, actualListScaling;

	protected int defaultColour = -1;
	protected double percentageFill = 1;
	protected boolean updateScaling = true;

	public DisplayElementList(double width, double height, double maxScale) {
		maxListScaling = new double[] { width, height, maxScale };
		elements = Lists.newArrayList();
	}

	public IDisplayElement addElement(IDisplayElement element) {
		elements.add(element);
		this.updateScaling = true;
		return element;
	}

	public IDisplayElement removeElement(IDisplayElement element) {
		elements.remove(element);
		this.updateScaling = true;
		return element;
	}

	public IDisplayElement getElement(int pos) {
		return elements.get(pos);
	}

	public int setDefaultColour(int colour) {
		return defaultColour = colour;
	}

	public double setPercentageFill(double fill) {
		this.updateScaling = true;
		return percentageFill = fill;
	}

	@Override
	public void updateRender() {
		getElements().forEach(IDisplayElement::updateRender);
		if (updateScaling) {
			updateActualScaling();
			updateScaling = !updateScaling;
		}
	}

	@Override
	public void render() {
		InfoRenderer.align(maxListScaling, actualListScaling, WidthAlignment.LEFT, HeightAlignment.CENTERED);
		InfoRenderer.renderDisplayElements(actualListScaling, maxListScaling, percentageFill, elements);
	}

	@Override
	public double[] getMaxListScaling() {
		return maxListScaling;
	}

	@Override
	public final int getDefaultColour() {
		return defaultColour;
	}

	@Override
	public void doDefaultRender(IDisplayElement element) {
		FontHelper.text(element.getRepresentiveString(), 0, 0, getDefaultColour());
	}

	public void updateActualScaling() {
		double[] listScaling = new double[3];

		for (IDisplayElement e : elements) {
			e.setMaxScaling(createMaxScaling(e));
			double[] scaling = e.getMaxScaling();

			for (int i = 0; i < 3; i++) {
				double lValue = listScaling[i];
				double eValue = scaling[i];
				switch (i) {
				case WIDTH: // width
					if (lValue < eValue) {
						listScaling[i] = eValue;
					}
					break;
				case HEIGHT: // height
					listScaling[i] += eValue;
					// add spacing?
					continue;
				case SCALE: // scale
					if (lValue < eValue) {
						listScaling[i] = eValue;
					}
					break;
				}
			}
		}
		actualListScaling = listScaling;
		int count = 0;
		for (IDisplayElement e : elements) {
			if (count != elements.size() - 1) {
				//listScaling[HEIGHT] += 0.0625;//e.setMaxScaling(createActualScaling(e))[2];
			}
			count++;
		}

	}

	@Override
	public double[] createMaxScaling(IDisplayElement element) {
		double fill = element.getPercentageFill() == 0 ? percentageFill : element.getPercentageFill();
		double maxIndividualHeight = maxListScaling[HEIGHT] / elements.size();
		return InfoRenderer.getScaling(element.getUnscaledWidthHeight(), new double[] { maxListScaling[0], maxIndividualHeight, maxListScaling[2] }, fill);
	}

	@Override
	public double[] createActualScaling(IDisplayElement element) {
		double maxIndividualHeight = maxListScaling[HEIGHT] / elements.size();
		return InfoRenderer.getScaling(element.getUnscaledWidthHeight(), new double[] { actualListScaling[0], maxIndividualHeight, actualListScaling[2] }, 1);
	}

	@Override
	public void onInfoUUIDChanged(InfoUUID id) {

	}

	@Override
	public final List<IDisplayElement> getElements() {
		return elements;
	}

	@Override
	public double[] setMaxScaling(double[] scaling) {
		this.updateScaling = true;
		return this.maxListScaling = scaling;
	}

	@Override
	public void onElementChanged(IDisplayElement element) {
		this.updateScaling = true;
	}

}
