package sonar.logistics.api.displays.elements.types;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.elements.AbstractDisplayElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IInfoReferenceElement;
import sonar.logistics.api.displays.elements.ILookableElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gui.display.GuiUnconfiguredInfoElement;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

@DisplayElementType(id = UnconfiguredInfoElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class UnconfiguredInfoElement extends AbstractDisplayElement implements ILookableElement, IClickableElement, IInfoReferenceElement, IFlexibleGui {

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
		for (IDisplayElement e : elements) {
			pushMatrix();
			DisplayElementHelper.align(getHolder().getAlignmentTranslation(e));
			double scale = e.getActualScaling()[SCALE];
			scale(scale, scale, scale);
			e.render();
			popMatrix();
		}
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
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
		if (elements == null) {
			return -1;
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
		return id == 0 ? new ContainerMultipartSync((TileSonarMultipart) obj) : null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
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

}
