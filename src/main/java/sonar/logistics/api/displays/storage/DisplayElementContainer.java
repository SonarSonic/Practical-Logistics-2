package sonar.logistics.api.displays.storage;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IDisplayRenderable;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InteractionHelper;

public class DisplayElementContainer implements IElementStorageHolder, INBTSyncable {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	public ElementStorage elements = new ElementStorage(this);
	public double[] createdTranslation = new double[] { 0, 0, 0 }; // in the form of values of up to 100 for the position.
	public double[] createdScaling = new double[] { 0, 0, 0 };
	public boolean locked = false; // if this is true the translation and max scaling will remain the same, even when new displays added.

	public double[] translation;
	private double[] actualContainerScaling; // only for aligning purposes.
	private double[] maxContainerScaling, maxElementScaling;
	public double percentageScale;
	protected int defaultColour = -1;
	public int containerIdentity;
	public DisplayGSI gsi;
	public boolean isWithinScreenBounds;

	public DisplayElementContainer() {}

	public DisplayElementContainer(DisplayGSI gsi, double xPos, double yPos, double zPos, double width, double height, double pScale, int identity) {
		this.gsi = gsi;
		resize(new double[] { xPos, yPos, zPos }, new double[] { width, height, 1 }, pScale);
		percentageScale = pScale;
		containerIdentity = identity;
	}

	public DisplayElementContainer(DisplayGSI gsi, double[] translate, double[] scale, double pScale, int identity) {
		this.gsi = gsi;
		resize(translate, scale, pScale);
		containerIdentity = identity;
	}
	
	/**for use for guide example configurations*/
	public DisplayElementContainer(double[] translate, double[] scale, double pScale, int identity, double[] display_scaling){
		createdTranslation = DisplayElementHelper.percentageFromScale(translate, display_scaling);
		createdScaling = DisplayElementHelper.percentageFromScale(scale, display_scaling);
		percentageScale = pScale;
		translation = null;
		maxElementScaling = null;
		maxElementScaling = null;
		containerIdentity = identity;
	}

	public void resize(double[] translate, double[] scale, double pScale) {
		createdTranslation = DisplayElementHelper.percentageFromScale(translate, gsi.getDisplayScaling());
		createdScaling = DisplayElementHelper.percentageFromScale(scale, gsi.getDisplayScaling());
		percentageScale = pScale;
		translation = null;
		maxElementScaling = null;
		maxElementScaling = null;
	}

	public DisplayGSI getGSI() {
		return gsi;
	}

	public boolean canRender() {
		return isWithinScreenBounds && !gsi.isGridSelectionMode;
	}

	public void render() {
		if (canRender()) {
			pushMatrix();
			translate(getTranslation()[0], getTranslation()[1], getTranslation()[2]);

			elements.forEach(IDisplayRenderable::updateRender);
			DisplayElementHelper.align(getAlignmentTranslation());
			DisplayElementHelper.renderElementStorageHolder(this);
			if (!gsi.isEditContainer(this)) {
				translate(0, 0, -0.02);
				if (gsi.isElementSelectionMode) {
					if (gsi.selection_mode.selected_identities.contains(getContainerIdentity())) {
						DisplayElementHelper.drawRect(0, 0, getContainerMaxScaling()[0], getContainerMaxScaling()[1], gsi.selection_mode.selectionType.getTypeColour());
					}
				}
				/* translate(0, 0, -0.002); CustomColour green = new CustomColour(255, 255, 255); double borderWidth = 0.0625 / 8; DisplayElementHelper.drawRect(0, 0, getContainerMaxScaling()[0], borderWidth, green.getRGB()); DisplayElementHelper.drawRect(0, getContainerMaxScaling()[1] - borderWidth, getContainerMaxScaling()[0], getContainerMaxScaling()[1], green.getRGB()); DisplayElementHelper.drawRect(0, 0, borderWidth, getContainerMaxScaling()[1], green.getRGB()); DisplayElementHelper.drawRect(getContainerMaxScaling()[0] - borderWidth, 0, getContainerMaxScaling()[0], getContainerMaxScaling()[1], green.getRGB()); */
			}
			popMatrix();
		}
	}

	@Override
	public void startElementRender(IDisplayElement e) {}

	@Override
	public void endElementRender(IDisplayElement e) {}

	public List<InfoUUID> getInfoReferences() {
		List<InfoUUID> uuid = new ArrayList<>();
		for (IDisplayElement s : elements) {
			ListHelper.addWithCheck(uuid, s.getInfoReferences());
		}
		return uuid;
	}
	public Tuple<IDisplayElement, double[]> getClickBoxes(double x, double y) {
		return getClickBoxes(getAlignmentTranslation(), x, y);
	}

	public Tuple<IDisplayElement, double[]> getClickBoxes(double[] align, double x, double y) {
		for (IDisplayElement e : elements) {
			if (!(e instanceof IElementStorageHolder)) {
				double[] alignArray = getAlignmentTranslation(e);
				double startX = align[0] + alignArray[0];
				double startY = align[1] + alignArray[1];
				double endX = align[0] + alignArray[0] + e.getActualScaling()[0];
				double endY = align[1] + alignArray[1] + e.getActualScaling()[1];
				double[] eBox = new double[] { startX, startY, endX, endY };
				if (InteractionHelper.checkClick(x, y, eBox)) {
					double subClickX = x - startX;
					double subClickY = y - startY;
					return new Tuple(e, new double[] { subClickX, subClickY });
				}
			}
		}
		for (IElementStorageHolder h : elements.getSubHolders()) {
			Tuple<IDisplayElement, double[]> clicked = h.getClickBoxes(x, y);
			if (clicked != null) {
				return clicked;
			}
		}
		return null;
	}
	
	public void updateActualScaling() {
		translation = null;
		maxContainerScaling = null;
		maxElementScaling = null;
		elements.forEach(e -> {
			if (e instanceof IElementStorageHolder) {
				((IElementStorageHolder) e).updateActualScaling();
			} else {
				e.onElementChanged();
			}
		});

		double[] listScaling = new double[3];

		for (IDisplayElement e : elements) {
			double[] scaling = e.getActualScaling();
			for (int i = 0; i < 3; i++) {
				double lValue = listScaling[i];
				double eValue = scaling[i];
				if (lValue < eValue) {
					listScaling[i] = eValue;
				}
			}
		}
		actualContainerScaling = listScaling;

		if (locked) {
			double endX = getTranslation()[0] + getActualScaling()[0];
			double endY = getTranslation()[1] + getActualScaling()[1];
			isWithinScreenBounds = endX <= gsi.getDisplayScaling()[0] && endY <= gsi.getDisplayScaling()[1];
		} else {
			isWithinScreenBounds = true;
		}

	}

	public boolean canClickContainer(double x, double y) {
		double startX = getTranslation()[0];
		double startY = getTranslation()[1];
		double endX = getTranslation()[0] + getContainerMaxScaling()[0];
		double endY = getTranslation()[1] + getContainerMaxScaling()[1];
		return InteractionHelper.checkClick(x, y, new double[] { startX, startY, endX, endY });
	}

	public Tuple<IDisplayElement, double[]> getElementFromXY(double x, double y) {
		double offsetX = x - getTranslation()[0];
		double offsetY = y - getTranslation()[1];
		return getClickBoxes(offsetX, offsetY);
	}

	@Override
	public DisplayElementContainer getContainer() {
		return this;
	}

	public double[] getTranslation() {
		if (translation == null) {
			if (locked) {
				translation = createdTranslation;// DisplayElementHelper.toNearestPixel(createdTranslation, gsi.getDisplayScaling());
			} else {
				translation = DisplayElementHelper.scaleFromPercentage(createdTranslation, gsi.getDisplayScaling());
			}
		}
		return translation;
	}

	/** the maximum scale of an element */
	public double[] getMaxScaling() {
		if (maxElementScaling == null) {
			maxElementScaling = DisplayElementHelper.scaleArray(getContainerMaxScaling(), percentageScale);
			maxElementScaling[SCALE] = 1;
		}
		return maxElementScaling;
	}

	/** the maximum scale of this container */
	public double[] getContainerMaxScaling() {
		if (maxContainerScaling == null) {
			if (locked) {
				maxContainerScaling = createdScaling;
			} else {
				maxContainerScaling = DisplayElementHelper.scaleFromPercentage(createdScaling, gsi.getDisplayScaling());
			}
			maxContainerScaling[SCALE] = 1;
		}
		return maxContainerScaling;
	}

	public double[] getActualScaling() {
		if (actualContainerScaling == null) {
			updateActualScaling();
		}
		return actualContainerScaling;
	}

	@Override
	public ElementStorage getElements() {
		return elements;
	}

	public void onElementAdded(IDisplayElement e) {
		e.setHolder(this);
		updateActualScaling();
		gsi.onElementAdded(this, e);
	}

	public void onElementRemoved(IDisplayElement element) {
		element.setHolder(this);
		updateActualScaling();
		gsi.onElementRemoved(this, element);
	}

	public int getContainerIdentity() {
		return containerIdentity;
	}

	public int setDefaultColour(int colour) {
		return defaultColour = colour;
	}

	public final int getDefaultColour() {
		return defaultColour;
	}

	public void doDefaultRender(IDisplayElement element) {
		FontHelper.text(element.getRepresentiveString(), 0, 0, getDefaultColour());
	}

	public void onInfoUUIDChanged(InfoUUID id) {}

	public void lock() {
		// when the display is locked we store the exact scale of the element
		createdTranslation = DisplayElementHelper.scaleFromPercentage(createdTranslation, gsi.getDisplayScaling());
		createdScaling = DisplayElementHelper.scaleFromPercentage(createdScaling, gsi.getDisplayScaling());
		translation = null;
		maxContainerScaling = null;
		locked = true;
	}

	public void unlock() {
		// when the display is unlocked we store a percentage of the element in relation to the display
		createdTranslation = DisplayElementHelper.percentageFromScale(createdTranslation, gsi.getDisplayScaling());
		createdScaling = DisplayElementHelper.percentageFromScale(createdScaling, gsi.getDisplayScaling());
		translation = null;
		maxContainerScaling = null;
		locked = false;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		containerIdentity = nbt.getInteger("iden");
		locked = nbt.getBoolean("locked");
		percentageScale = nbt.getDouble("percent");
		createdTranslation = NBTHelper.readDoubleArray(nbt, "c_trans", 3);
		createdScaling = NBTHelper.readDoubleArray(nbt, "c_scale", 3);
		elements.readData(nbt, type);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger("iden", containerIdentity);
		nbt.setBoolean("locked", locked);
		nbt.setDouble("percent", percentageScale);
		NBTHelper.writeDoubleArray(nbt, createdTranslation, "c_trans");
		NBTHelper.writeDoubleArray(nbt, createdScaling, "c_scale");
		elements.writeData(nbt, type);
		return nbt;
	}

	@Override
	public double[] createMaxScaling(IDisplayElement element) {
		switch (element.getFillType()) {
		case FILL_CONTAINER:
			return getContainerMaxScaling();
		case FILL_SCALED_CONTAINER:
			return getMaxScaling();
		default:
			return DisplayElementHelper.getScaling(element.getUnscaledWidthHeight(), getMaxScaling(), 1);
		}
	}

	@Override
	public double[] createActualScaling(IDisplayElement element) {
		switch (element.getFillType()) {
		case FILL_CONTAINER:
			return getContainerMaxScaling();
		case FILL_SCALED_CONTAINER:
			return getMaxScaling();
		default:
			return DisplayElementHelper.getScaling(element.getUnscaledWidthHeight(), element.getMaxScaling(), 1);
		}
	}

	@Override
	public double[] getAlignmentTranslation() {
		return DisplayElementHelper.alignArray(getContainerMaxScaling(), getMaxScaling(), WidthAlignment.LEFT, HeightAlignment.TOP);
	}

	public double[] getFullAlignmentTranslation(IDisplayElement e) {
		double[] containerAlign = getAlignmentTranslation();
		double[] holderAlign = new double[] { 0, 0, 0 };
		double[] elementAlign = e.getHolder().getAlignmentTranslation(e);
		if ((e.getHolder() instanceof IDisplayElement)) {
			holderAlign = getAlignmentTranslation((IDisplayElement) e.getHolder());
		}
		double fullX = containerAlign[WIDTH] + holderAlign[WIDTH] + elementAlign[WIDTH];
		double fullY = containerAlign[HEIGHT] + holderAlign[HEIGHT] + elementAlign[HEIGHT];
		return new double[] { fullX, fullY, elementAlign[SCALE] };
	}

	@Override
	public double[] getAlignmentTranslation(IDisplayElement e) {
		return e.getAlignmentTranslation(getContainerMaxScaling(), e.getActualScaling());
	}

}
