package sonar.logistics.api.displays.storage;

import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import sonar.core.helpers.ListHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractDisplayElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InteractionHelper;

@DisplayElementType(id = DisplayElementList.REGISTRY_NAME, modid = PL2Constants.MODID)
//FIXME make it be able to do grids also.
public class DisplayElementList extends AbstractDisplayElement implements IElementStorageHolder {

	public boolean updateScaling = true;
	public boolean uniformScaling = true;
	public double minScale = 1;
	public ElementStorage elements = new ElementStorage(this);
	public List<InfoUUID> references = new ArrayList<>();

	public DisplayElementList() {
		super();
	}

	public DisplayElementList(IElementStorageHolder holder) {
		super();
		setHolder(holder);
	}

	@Override
	public ElementStorage getElements() {
		return elements;
	}

	@Override
	public DisplayElementContainer getContainer() {
		return holder.getContainer();
	}

	public void onElementAdded(IDisplayElement element) {
		element.setHolder(this);
		updateScaling = true;
		getGSI().onElementAdded(this, element);		
	}

	public void onElementRemoved(IDisplayElement element) {
		element.setHolder(this);
		updateScaling = true;
		getGSI().onElementRemoved(this, element);
	}

	public void onElementChanged() {
		elements.forEach(IDisplayElement::onElementChanged);
		updateActualScaling();
	}

	public void updateRender() {
		elements.forEach(IDisplayElement::updateRender);

		if (updateScaling) {
			updateActualScaling();
			updateScaling = !updateScaling;
		}
	}

	public void render() {
		DisplayElementHelper.renderElementStorageHolder(this);
	}

	@Override
	public void startElementRender(IDisplayElement e) {}

	@Override
	public void endElementRender(IDisplayElement e) {
		translate(0, e.getActualScaling()[HEIGHT], 0);
	}

	@Override
	public Tuple<IDisplayElement, double[]> getClickBoxes(double x, double y) {
		double[] align = holder.getAlignmentTranslation(this);
		Map<IDisplayElement, Double[]> boxes = new HashMap<>();
		double heightOffset = 0;
		for (IDisplayElement e : elements) { // we can't only iterate over clickables because we need the heightOffset
			if (e instanceof IElementStorageHolder) {
				Tuple<IDisplayElement, double[]> clicked = ((IElementStorageHolder) e).getClickBoxes(x, y); // FIXME sub storage holders may not have heightoffset accounted for
				if (clicked != null) {
					return clicked;
				}
			} else {// if (e instanceof IClickableElement) {
				//double[] alignArray = InfoRenderer.alignArray(new double[] { getMaxScaling()[0], e.getMaxScaling()[1], e.getMaxScaling()[2] }, e.getActualScaling(), e.getWidthAlignment(), e.getHeightAlignment());
				double[] alignArray = getAlignmentTranslation(e);
				double startX = align[WIDTH] + alignArray[WIDTH];
				double startY = align[HEIGHT] + alignArray[HEIGHT] + heightOffset;
				double endX = align[WIDTH] + alignArray[WIDTH] + e.getActualScaling()[WIDTH];
				double endY = align[HEIGHT] + alignArray[HEIGHT] + e.getActualScaling()[HEIGHT] + heightOffset;
				double[] eBox = new double[] { startX, startY, endX, endY };
				if (InteractionHelper.checkClick(x, y, eBox)) {
					double subClickX = x - startX;
					double subClickY = y - startY;
					return new Tuple(e, new double[]{subClickX, subClickY});
				}
			}
			heightOffset += e.getActualScaling()[1];
		}
		return null;
	}

	public void updateActualScaling() {
		double[] listScaling = new double[3];
		double minScale = 1;
		listScaling[SCALE] = 1;
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
					if (minScale > eValue) {
						minScale = eValue;
					}
					break;
				}
			}
		}
		this.minScale = minScale;
		
		if (!uniformScaling) {
			setActualScaling(listScaling);
		} else {
			double[] uniformScaling = new double[3];
			uniformScaling[SCALE] = 1;

			for (IDisplayElement e : elements) {
				e.setActualScaling(createActualScaling(e));
				double[] scaling = e.getActualScaling();

				for (int i = 0; i < 3; i++) {
					double lValue = uniformScaling[i];
					double eValue = scaling[i];
					switch (i) {
					case WIDTH: // width
						if (lValue < eValue) {
							uniformScaling[i] = eValue;
						}
						break;
					case HEIGHT: // height
						uniformScaling[i] += eValue;
						// add spacing?
						continue;
					case SCALE: // scale
						if (minScale > eValue) {
							minScale = eValue;
						}
						break;
					}
				}
			}
			setActualScaling(uniformScaling);

		}
		/* int count = 0; for (IDisplayElement e : elements) { if (count != elements.getElementCount() - 1) { // listScaling[HEIGHT] += 0.0625;//e.setMaxScaling(createActualScaling(e))[2]; } count++; } */

	}

	@Override
	public double[] getMaxScaling() {
		double[] maxScale = getHolder().getMaxScaling();
		maxScale[SCALE] = 1;
		return maxScale;
	}

	public double[] createMaxScaling(IDisplayElement element) {
		double fill = element.getPercentageFill() == 0 ? percentageFill : element.getPercentageFill();
		double maxIndividualHeight = getMaxScaling()[HEIGHT] / elements.getElementCount();
		double[] maxScaling = DisplayElementHelper.getScaling(element.getUnscaledWidthHeight(), new double[] { getMaxScaling()[0], maxIndividualHeight, getMaxScaling()[2] }, fill);
		return maxScaling;
	}

	public double[] createActualScaling(IDisplayElement element) {		
		if (uniformScaling) {
			double actualElementScale = minScale;
			double actualElementWidth = (element.getUnscaledWidthHeight()[0] * actualElementScale) * percentageFill;
			double actualElementHeight = (element.getUnscaledWidthHeight()[1] * actualElementScale) * percentageFill;
			return new double[] { actualElementWidth, actualElementHeight, actualElementScale };
		}
		double maxIndividualHeight = holder.getActualScaling()[HEIGHT] / elements.getElementCount();
		return DisplayElementHelper.getScaling(element.getUnscaledWidthHeight(), new double[] { getActualScaling()[0], maxIndividualHeight, getActualScaling()[2] }, 1);
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		List<InfoUUID> uuid = new ArrayList<>();
		for(IDisplayElement s : elements){
			ListHelper.addWithCheck(uuid, s.getInfoReferences());
		}		
		return uuid; // FIXME CACHE THIS?
	}

	@Override
	public String getRepresentiveString() {
		return REGISTRY_NAME;// this shouldn't be used when rendering the list
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		elements.readData(nbt, type);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		elements.writeData(nbt, type);
		return nbt;
	}

	@Override
	public int[] createUnscaledWidthHeight() {
		return new int[] { 0, 0 };
	}

	public static final String REGISTRY_NAME = "element_list";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

	@Override
	public double[] getAlignmentTranslation() {
		return DisplayElementHelper.alignArray(holder.getMaxScaling(), getActualScaling(), width_align, height_align);
	}

	@Override
	public double[] getAlignmentTranslation(IDisplayElement e) {
		return DisplayElementHelper.alignArray(new double[] { getActualScaling()[0], e.getMaxScaling()[1], e.getMaxScaling()[2] }, e.getActualScaling(), e.getWidthAlignment(), e.getHeightAlignment());
	}
}
