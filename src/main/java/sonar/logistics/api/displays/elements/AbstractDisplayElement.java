package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.IDisplayElementList;

public abstract class AbstractDisplayElement implements IDisplayElement {

	public IDisplayElementList list;
	public double[] maxScaling, actualScaling;
	public int[] unscaledWidthHeight;
	public double percentageFill = 1;
	public WidthAlignment width_align = WidthAlignment.CENTERED;
	public HeightAlignment height_align = HeightAlignment.CENTERED;

	public AbstractDisplayElement(IDisplayElementList list) {
		this.list = list;
	}

	@Override
	public final IDisplayElementList getList() {
		return list;
	}

	@Override
	public void onElementChanged() {
		unscaledWidthHeight = null;
		maxScaling = null;
		actualScaling = null;
		list.onElementChanged(this);
	}

	@Override
	public WidthAlignment getWidthAlignment(){
		return width_align;
	}

	@Override
	public WidthAlignment setWidthAlignment(WidthAlignment align){
		return width_align = align;
	}

	@Override
	public HeightAlignment getHeightAlignment(){
		return height_align;
	}

	@Override
	public HeightAlignment setHeightAlignment(HeightAlignment align){
		return height_align = align;
	}

	@Override
	public double getPercentageFill() {
		return percentageFill;
	}

	public double setPercentageFill(double fill) {
		onElementChanged();
		return percentageFill = fill;
	}

	@Override
	public final double[] getMaxScaling() {
		if (maxScaling == null) {
			maxScaling = list.createMaxScaling(this);
		}
		return maxScaling;
	}

	@Override
	public double[] setMaxScaling(double[] scaling) {
		return this.maxScaling = scaling;
	}

	@Override
	public final double[] getActualScaling() {
		if (actualScaling == null) {
			actualScaling = list.createActualScaling(this);
		}
		return actualScaling;
	}

	@Override
	public double[] setActualScaling(double[] scaling) {
		return this.actualScaling = scaling;
	}

	@Override
	public final int[] getUnscaledWidthHeight() {
		if (unscaledWidthHeight == null) {
			unscaledWidthHeight = createUnscaledWidthHeight();
		}
		return unscaledWidthHeight;
	}

	abstract int[] createUnscaledWidthHeight();

}
