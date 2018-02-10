package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.IDisplayElement;

public abstract class AbstractDisplayElement implements IDisplayElement {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	public DisplayElementContainer container;
	public double[] maxScaling, actualScaling;
	public int[] unscaledWidthHeight;
	public double percentageFill = 1;
	public WidthAlignment width_align = WidthAlignment.CENTERED;
	public HeightAlignment height_align = HeightAlignment.CENTERED;

	public AbstractDisplayElement() {}
	
	public AbstractDisplayElement(DisplayElementContainer list) {
		this.container = list;
	}
	
	public final DisplayElementContainer setContainer(DisplayElementContainer c) {
		return container = c;
	}

	@Override
	public final DisplayElementContainer getContainer() {
		return container;
	}

	@Override
	public void onElementChanged() {
		unscaledWidthHeight = null;
		maxScaling = null;
		actualScaling = null;
		container.onElementChanged(this);
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
		return maxScaling;
	}

	@Override
	public double[] setMaxScaling(double[] scaling) {
		return this.maxScaling = scaling;
	}

	@Override
	public final double[] getActualScaling() {
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
