package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.ElementFillType;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.InfoError;

@DisplayElementType(id = UnconfiguredInfoElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class UnconfiguredInfoElement extends AbstractDisplayElement {

	public List<IDisplayElement> elements;
	public InfoUUID uuid;

	public UnconfiguredInfoElement(){
		this.width_align = WidthAlignment.LEFT;
		this.height_align = HeightAlignment.TOP;
	}
	
	public UnconfiguredInfoElement(InfoUUID uuid){
		this.uuid=uuid;		
		this.width_align = WidthAlignment.LEFT;
		this.height_align = HeightAlignment.TOP;
	}
	public void updateRender() {
		if (elements == null) {
			updateInfoElements();
		}
		elements.forEach(IDisplayElement::updateRender);
	}

	public void render() {		
		for (IDisplayElement e : elements) {
			pushMatrix();
			InfoRenderer.align(getHolder().getAlignmentTranslation(e));
			double scale = e.getActualScaling()[SCALE];
			scale(scale, scale, scale);
			e.render();
			popMatrix();
		}
		
	}

	@Override
	public void onElementChanged() {
		super.onElementChanged();
		elements = null;
	}

	public void updateInfoElements() {
		
		IInfo info = getGSI().getCachedInfo(uuid);
		if (info == null) {
			elements = Lists.newArrayList();
			return;
			
		}
		List<IDisplayElement> nElements = Lists.newArrayList();
		info.createDefaultElements(nElements, getHolder(), uuid);
		nElements.forEach(e -> e.setHolder(getHolder()));
		elements = nElements;
		
		
	}

	public List<IDisplayElement> getInfoElements() {
		if (elements == null) {
			updateInfoElements();
		}
		return elements;
	}
	
	@Override
	public double[] getMaxScaling() {
		return new double[]{getHolder().getContainer().getContainerMaxScaling()[WIDTH], getHolder().getMaxScaling()[HEIGHT], 1};
	}

	@Override
	public double[] getActualScaling() {
		return getMaxScaling();
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		return Lists.newArrayList(uuid);
	}

	@Override
	public String getRepresentiveString() {
		return uuid.toString();
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_CONTAINER;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		uuid = NBTHelper.instanceNBTSyncable(InfoUUID.class, nbt);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		uuid.writeData(nbt, type);
		return nbt;
	}

	public static final String REGISTRY_NAME = "u_info";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
