package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.IDisplayElement;

public abstract class AbstractDisplayElement implements IDisplayElement {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	public IElementStorageHolder holder;
	private double[] maxScaling, actualScaling;
	private int[] unscaledWidthHeight;
	protected double percentageFill = 1;
	protected WidthAlignment width_align = WidthAlignment.CENTERED;
	protected HeightAlignment height_align = HeightAlignment.CENTERED;

	public AbstractDisplayElement() {}
	
	public final IElementStorageHolder setHolder(IElementStorageHolder c) {
		return holder = c;
	}

	@Override
	public IElementStorageHolder getHolder() {
		return holder;
	}

	@Override
	public void onElementChanged() {
		unscaledWidthHeight = null;
		maxScaling = null;
		actualScaling = null;
		//holder.updateActualScaling();
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
	public double[] getMaxScaling() {
		if(maxScaling==null){
			maxScaling = getHolder().createMaxScaling(this);
		}
		return maxScaling;
	}

	@Override
	public double[] setMaxScaling(double[] scaling) {
		return this.maxScaling = scaling;
	}

	@Override
	public double[] getActualScaling() {
		if(actualScaling==null){
			actualScaling = getHolder().createActualScaling(this);
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
