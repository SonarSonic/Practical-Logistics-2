package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.IDisplayRenderable;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.helpers.InteractionHelper;

public class DisplayElementContainer implements IElementStorageHolder, INBTSyncable {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	public ElementStorage elements = new ElementStorage(this);
	private double[] createdTranslation = new double[] { 0, 0, 0 }; // in the form of values of up to 100 for the position.
	private double[] createdScaling = new double[] { 0, 0, 0 };
	public boolean locked = false; // if this is true the translation and max scaling will remain the same, even when new displays added.

	private double[] translation;
	private double[] actualContainerScaling; // only for aligning purposes.
	private double[] maxContainerScaling;
	private double percentageScale;
	protected int defaultColour = -1;
	public int containerIdentity;
	public DisplayGSI gsi;
	public boolean isWithinScreenBounds;

	public DisplayElementContainer() {}

	public DisplayElementContainer(DisplayGSI gsi, double xPos, double yPos, double zPos, double width, double height, double pScale, int identity) {
		this.gsi = gsi;
		createdTranslation = DisplayElementHelper.percentageFromScale(new double[] { xPos, yPos, zPos }, gsi.getDisplayScaling());
		createdScaling = DisplayElementHelper.percentageFromScale(new double[] { width, height, 1 }, gsi.getDisplayScaling());
		percentageScale = pScale;
		containerIdentity = identity;
	}

	public DisplayElementContainer(DisplayGSI gsi, double[] translate, double[] scale, double pScale, int identity) {
		this.gsi = gsi;
		createdTranslation = DisplayElementHelper.percentageFromScale(translate, gsi.getDisplayScaling());
		createdScaling = DisplayElementHelper.percentageFromScale(scale, gsi.getDisplayScaling());
		percentageScale = pScale;
		containerIdentity = identity;
	}

	public boolean canRender() {
		return isWithinScreenBounds && (gsi.isEditContainer(this) ? gsi.edit_mode.getObject() && !gsi.isGridSelectionMode : true);
	}

	public void render() {
		if (canRender()) {
			pushMatrix();
			translate(getTranslation()[0], getTranslation()[1], getTranslation()[2]);
			elements.forEach(IDisplayRenderable::updateRender);
			InfoRenderer.align(getAlignmentTranslation());
			InfoRenderer.renderElementStorageHolder(this);
			popMatrix();
		}
	}

	@Override
	public void startElementRender(IDisplayElement e) {}

	@Override
	public void endElementRender(IDisplayElement e) {}

	public Tuple<IDisplayElement, double[]> getClickBoxes(double x, double y) {
		double[] align = getAlignmentTranslation();
		Map<IDisplayElement, Double[]> boxes = Maps.newHashMap();
		for (IDisplayElement e : elements) {// FIXME
			// for (IDisplayElement e : elements.getClickables()) {
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
		double endX = getTranslation()[0] + getMaxScaling()[0];
		double endY = getTranslation()[1] + getMaxScaling()[1];
		return InteractionHelper.checkClick(x, y, new double[] { startX, startY, endX, endY });
	}

	public Tuple<IDisplayElement, double[]> getClickedElement(double x, double y) {
		double offsetX = x - getTranslation()[0];
		double offsetY = y - getTranslation()[1];
		/* for (IDisplayElement e : elements) { if (checkClick(offsetX, offsetY, e.getClickBox())) { return e; } } */
		return getClickBoxes(x, y);
	}

	@Override
	public DisplayElementContainer getContainer() {
		return this;
	}

	public double[] getTranslation() {
		if (translation == null) {
			if (locked) {
				translation = DisplayElementHelper.toNearestPixel(createdTranslation, gsi.getDisplayScaling());
			} else {
				translation = DisplayElementHelper.scaleFromPercentage(createdTranslation, gsi.getDisplayScaling());
			}
		}
		return translation;
	}

	public double[] getActualScaling() {
		if (actualContainerScaling == null) {
			updateActualScaling();
		}
		return actualContainerScaling;
	}

	public double[] getMaxScaling() {
		if (maxContainerScaling == null) {
			if (locked) {
				maxContainerScaling = DisplayElementHelper.toNearestPixel(createdScaling, gsi.getDisplayScaling());
			} else {
				maxContainerScaling = DisplayElementHelper.toNearestPixel(DisplayElementHelper.scaleFromPercentage(createdScaling, gsi.getDisplayScaling()), gsi.getDisplayScaling());
			}

			// listScaling = DisplayElementHelper.scale(listScaling, percentageScale);
			// listScaling[SCALE] = 1;
		}
		return maxContainerScaling;
	}

	@Override
	public ElementStorage getElements() {
		return elements;
	}

	public void onElementAdded(IDisplayElement element) {
		element.setHolder(this);
		updateActualScaling();
	}

	public void onElementRemoved(IDisplayElement element) {
		element.setHolder(this);
		updateActualScaling();
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

	public void onInfoUUIDChanged(InfoUUID id) {

	}

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
		locked = nbt.getBoolean("locked");
		percentageScale = nbt.getDouble("percent");
		createdTranslation = NBTHelper.readDoubleArray(nbt, "c_trans", 3);
		createdScaling = NBTHelper.readDoubleArray(nbt, "c_scale", 3);
		containerIdentity = nbt.getInteger("iden");
		elements.readData(nbt, type);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setBoolean("locked", locked);
		nbt.setDouble("percent", percentageScale);
		NBTHelper.writeDoubleArray(nbt, createdTranslation, "c_trans");
		NBTHelper.writeDoubleArray(nbt, createdScaling, "c_scale");
		nbt.setInteger("iden", containerIdentity);
		elements.writeData(nbt, type);
		return nbt;
	}

	@Override
	public double[] createMaxScaling(IDisplayElement element) {
		return InfoRenderer.getScaling(element.getUnscaledWidthHeight(), getMaxScaling(), 1);
	}

	@Override
	public double[] createActualScaling(IDisplayElement element) {
		return InfoRenderer.getScaling(element.getUnscaledWidthHeight(), getMaxScaling(), 1);
	}

	@Override
	public double[] getAlignmentTranslation() {
		return InfoRenderer.alignArray(getMaxScaling(), getActualScaling(), WidthAlignment.LEFT, HeightAlignment.TOP);
	}

	@Override
	public double[] getAlignmentTranslation(IDisplayElement e) {
		return InfoRenderer.alignArray(getMaxScaling(), e.getActualScaling(), e.getWidthAlignment(), e.getHeightAlignment());
	}

}
