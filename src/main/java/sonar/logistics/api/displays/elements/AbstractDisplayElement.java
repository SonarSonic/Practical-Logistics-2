package sonar.logistics.api.displays.elements;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.IDisplayElement;

public abstract class AbstractDisplayElement implements IDisplayElement {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	public IElementStorageHolder holder;
	private double[] maxScaling, actualScaling;
	private int[] unscaledWidthHeight;
	protected double percentageFill = 1;
	protected WidthAlignment width_align = WidthAlignment.CENTERED;
	protected HeightAlignment height_align = HeightAlignment.CENTERED;
	private int identity = -1;

	public AbstractDisplayElement() {}

	public int getElementIdentity() {
		if (identity == -1) {
			identity = PL2.getServerManager().getNextIdentity();
		}
		return identity;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		identity = nbt.getInteger("identity");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger("identity", getElementIdentity());
		return nbt;
	}

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
	}

	@Override
	public WidthAlignment getWidthAlignment() {
		return width_align;
	}

	@Override
	public WidthAlignment setWidthAlignment(WidthAlignment align) {
		return width_align = align;
	}

	@Override
	public HeightAlignment getHeightAlignment() {
		return height_align;
	}

	@Override
	public HeightAlignment setHeightAlignment(HeightAlignment align) {
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
		if (maxScaling == null) {
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
		if (actualScaling == null) {
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

	int[] createUnscaledWidthHeight() {
		return new int[] { 1, 1 };
	}

}
