package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;

@DisplayElementType(id = DisplayElementList.REGISTRY_NAME, modid = PL2Constants.MODID)
public class DisplayElementList extends AbstractDisplayElement implements IElementStorageHolder {

	public boolean updateScaling = true;
	public ElementStorage elements = new ElementStorage(this);
	public List<InfoUUID> references = Lists.newArrayList();

	public DisplayElementList(DisplayElementContainer container) {
		super(container);
	}


	@Override
	public ElementStorage getElements() {
		return elements;
	}

	public void onElementAdded(IDisplayElement element) {
		this.updateScaling = true;
		element.setContainer(container);
	}

	public void onElementRemoved(IDisplayElement element) {
		this.updateScaling = true;
		element.setContainer(container);
	}

	public void onElementChanged(IDisplayElement element) {
		this.updateScaling = true;
		element.setContainer(container);
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
		InfoRenderer.align(container.maxContainerScaling, actualScaling, WidthAlignment.LEFT, HeightAlignment.CENTERED);
		InfoRenderer.renderDisplayElements(container.maxContainerScaling, actualScaling, percentageFill, elements);
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
		setActualScaling(listScaling);
		int count = 0;
		for (IDisplayElement e : elements) {
			if (count != elements.getElementCount() - 1) {
				// listScaling[HEIGHT] += 0.0625;//e.setMaxScaling(createActualScaling(e))[2];
			}
			count++;
		}

	}

	public double[] createMaxScaling(IDisplayElement element) {
		double fill = element.getPercentageFill() == 0 ? percentageFill : element.getPercentageFill();
		double maxIndividualHeight = container.maxContainerScaling[HEIGHT] / elements.getElementCount();
		return InfoRenderer.getScaling(element.getUnscaledWidthHeight(), new double[] { container.maxContainerScaling[0], maxIndividualHeight, container.maxContainerScaling[2] }, fill);
	}

	public double[] createActualScaling(IDisplayElement element) {
		double maxIndividualHeight = container.maxContainerScaling[HEIGHT] / elements.getElementCount();
		return InfoRenderer.getScaling(element.getUnscaledWidthHeight(), new double[] { actualScaling[0], maxIndividualHeight, actualScaling[2] }, 1);
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		return references; //FIXME
	}

	@Override
	public String getRepresentiveString() {
		return REGISTRY_NAME;// this shouldn't be used when rendering the list
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		elements.readData(nbt, type);		
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		elements.writeData(nbt, type);
		return nbt;
	}

	@Override
	int[] createUnscaledWidthHeight() {
		return new int[] { 0, 0 };
	}

	public static final String REGISTRY_NAME = "element_list";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
