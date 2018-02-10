package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;

public class DisplayElementContainer implements IElementStorageHolder, INBTSyncable {

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;
	public ElementStorage elements = new ElementStorage(this);
	protected double[] maxContainerScaling;
	protected int defaultColour = -1;
	public int containerIdentity;

	public DisplayElementContainer() {}

	public DisplayElementContainer(double width, double height, double maxScale, int identity) {
		maxContainerScaling = new double[] { width, height, maxScale };
		containerIdentity = identity;
	}

	@Override
	public ElementStorage getElements() {
		return elements;
	}

	public void onElementAdded(IDisplayElement element) {
		element.setContainer(this);
	}

	public void onElementRemoved(IDisplayElement element) {
		element.setContainer(this);
	}

	public void onElementChanged(IDisplayElement element) {
		element.setContainer(this);
	}

	public int getContainerIdentity() {
		return containerIdentity;
	}

	public int setDefaultColour(int colour) {
		return defaultColour = colour;
	}

	public double[] getMaxListScaling() {
		return maxContainerScaling;
	}

	public final int getDefaultColour() {
		return defaultColour;
	}

	public void doDefaultRender(IDisplayElement element) {
		FontHelper.text(element.getRepresentiveString(), 0, 0, getDefaultColour());
	}

	public void onInfoUUIDChanged(InfoUUID id) {

	}

	public double[] setMaxScaling(double[] scaling) {
		return this.maxContainerScaling = scaling;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		elements.readData(nbt, type);
		containerIdentity = nbt.getInteger("iden");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		elements.writeData(nbt, type);
		nbt.setInteger("iden", containerIdentity);
		return nbt;
	}

}
