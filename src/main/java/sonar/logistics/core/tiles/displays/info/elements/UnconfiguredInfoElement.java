package sonar.logistics.core.tiles.displays.info.elements;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMDisplayElement;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.requests.info.GuiInfoSource;
import sonar.logistics.base.requests.info.IInfoRequirement;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.gui.GuiUnconfiguredInfoElement;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIElementPacketHelper;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayGSISaveHandler;
import sonar.logistics.core.tiles.displays.info.InfoRenderHelper;
import sonar.logistics.core.tiles.displays.info.elements.base.*;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.renderer.GlStateManager.*;

@ASMDisplayElement(id = UnconfiguredInfoElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class UnconfiguredInfoElement extends AbstractDisplayElement implements ILookableElement, IClickableElement, IInfoReferenceElement, IInfoRequirement, IFlexibleGui {

	public List<IDisplayElement> elements;
	public InfoUUID uuid;

	public UnconfiguredInfoElement() {
		super();
		this.width_align = WidthAlignment.LEFT;
		this.height_align = HeightAlignment.TOP;
	}

	public UnconfiguredInfoElement(InfoUUID uuid) {
		this.uuid = uuid;
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
		if(getInfoElements().isEmpty()){
			InfoRenderHelper.renderCenteredStringsWithAdaptiveScaling(getActualScaling()[WIDTH], getActualScaling()[HEIGHT], getActualScaling()[SCALE], 0, 0.5, -1, Lists.newArrayList("NO DATA"));
		}else {
			for (IDisplayElement e : getInfoElements()) {
				pushMatrix();
				DisplayElementHelper.align(getHolder().getAlignmentTranslation(e));
				double scale = e.getActualScaling()[SCALE];
				scale(scale, scale, scale);
				e.render();
				popMatrix();
			}
		}
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		if(this.getInfoElements().isEmpty()){
			return IGuiOrigin.withOrigin(new GuiInfoSource(this, getGSI(), new ContainerMultipartSync(obj)), origin);
		}
		return new GuiUnconfiguredInfoElement(obj, this, origin);
	}

	@Override
	public void onElementChanged() {
		super.onElementChanged();
		elements = null;
	}

	@Override
	public void onInfoReferenceChanged(InfoUUID uuid, IInfo info) {
		if (this.uuid.equals(uuid)) {
			updateInfoElements();
			getInfoElements().stream().filter(e -> e instanceof IInfoReferenceElement)
			.forEach(e -> ((IInfoReferenceElement)e).onInfoReferenceChanged(this.uuid, info));
		}
	}

	public void updateInfoElements() {
		IInfo info = getGSI().getCachedInfo(uuid);
		if (info == null) {
			elements = new ArrayList<>();
			return;
		}
		List<IDisplayElement> nElements = new ArrayList<>();
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
		return new double[] { getHolder().getContainer().getContainerMaxScaling()[WIDTH], getHolder().getContainer().getContainerMaxScaling()[HEIGHT], 1 };
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
		return "Unconfigured: " + uuid.toString();
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_CONTAINER;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		(uuid = new InfoUUID()).readData(nbt, type);
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

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		if (this.getInfoElements().isEmpty()) {
			return -2;
		}
		for (IDisplayElement e : elements) {
			if (e instanceof IClickableElement) {
				double[] align = getHolder().getAlignmentTranslation(e);

				double subSubClickX = subClickX - align[0];
				double subSubClickY = subClickY - align[1];
				int gui = ((IClickableElement) e).onGSIClicked(click, player, subSubClickX, subSubClickY);
				if (gui != -1) {
					return gui;
				}
			}
		}

		return -1;
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		if (elements != null) {
			for (IDisplayElement e : elements) {
				if (e instanceof IFlexibleGui) {
					Object element = ((IFlexibleGui) e).getServerElement(obj, id, world, player, tag);
					if (element != null) {
						return element;
					}
				}
			}
		}
		return id == 0 || id == -2 ? new ContainerMultipartSync((TileSonarMultipart) obj) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		if(id == -2){
			return new GuiInfoSource(this, getGSI(), new ContainerMultipartSync((TileSonarMultipart) obj));
		}
		if (elements != null) {
			for (IDisplayElement e : elements) {
				if (e instanceof IFlexibleGui) {
					Object element = ((IFlexibleGui) e).getClientElement(obj, id, world, player, tag);
					if (element != null) {
						return element;
					}
				}
			}
		}
		return null;
	}

	// INFO SOURCE GUI \\

	@Override
	public int getRequired() {
		return 1;
	}

	@Override
	public List<InfoUUID> getSelectedInfo() {
		return getInfoReferences();
	}

	@Override
	public void onGuiClosed(List<InfoUUID> selected) {
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createInfoRequirementPacket(selected), getElementIdentity(), getGSI());
	}

	public void doInfoRequirementPacket(DisplayGSI gsi, EntityPlayer player, List<InfoUUID> require) {
		InfoUUID infoUUID = require.get(0);
		if(InfoUUID.valid(infoUUID)){
			this.uuid = infoUUID;
			gsi.sendInfoContainerPacket(DisplayGSISaveHandler.DisplayGSISavedData.ALL_DATA);
		}
	}
}
