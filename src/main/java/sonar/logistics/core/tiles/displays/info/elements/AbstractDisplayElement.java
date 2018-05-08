package sonar.logistics.core.tiles.displays.info.elements;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.core.tiles.displays.info.elements.base.HeightAlignment;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IElementStorageHolder;
import sonar.logistics.core.tiles.displays.info.elements.base.WidthAlignment;

public abstract class AbstractDisplayElement implements IDisplayElement {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	public IElementStorageHolder holder;
	protected double[] maxScaling, actualScaling;
	protected int[] unscaledWidthHeight;
	protected double percentageFill = 1;
	protected WidthAlignment width_align = WidthAlignment.CENTERED;
	protected HeightAlignment height_align = HeightAlignment.CENTERED;
	private int identity = -1;
	public static final String IDENTITY_TAG_NAME = "identity";

	public AbstractDisplayElement() {}

	public int getElementIdentity() {
		if (identity == -1) {
			identity = ServerInfoHandler.instance().getNextIdentity();
		}
		return identity;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		identity = nbt.getInteger(IDENTITY_TAG_NAME);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger(IDENTITY_TAG_NAME, getElementIdentity());
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

	public int[] createUnscaledWidthHeight() {
		return new int[] { 1, 1 };
	}

}
