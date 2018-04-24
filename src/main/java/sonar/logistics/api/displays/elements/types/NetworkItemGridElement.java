package sonar.logistics.api.displays.elements.types;

import static net.minecraft.client.renderer.GlStateManager.depthMask;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIClickPacketHelper;
import sonar.logistics.client.gui.display.GuiEditNetworkItemlist;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredItemStack;

@DisplayElementType(id = NetworkItemGridElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkItemGridElement extends NetworkGridElement<MonitoredItemStack> {

	public NetworkItemGridElement() {
		super();
	}

	public NetworkItemGridElement(InfoUUID uuid) {
		super(uuid);
	}

	public double getRenderWidth() {
		return Math.min(element_size, Math.min(getActualScaling()[0], getActualScaling()[1]));
	}

	public double getRenderHeight() {
		return Math.min(element_size, Math.min(getActualScaling()[0], getActualScaling()[1]));
	}

	public void renderGridElement(MonitoredItemStack stack, int index) {
		scale((width / 16) * grid_fill_percentage, (height / 16) * grid_fill_percentage, 0.001);
		disableLighting();
		RenderHelper.renderItemIntoGUI(stack.getItemStack(), 0, 0);
		translate(0, 0, 2);
		depthMask(false);
		RenderHelper.renderStoredItemStackOverlay(stack.getItemStack(), 0, 0, 0, text_colour, "" + stack.getStored(), false);
		depthMask(true);
		GlStateManager.color(1, 1, 1, 1);
	}

	public void onChangeableListChanged(InfoUUID uuid, AbstractChangeableList list) {
		if (info instanceof LogicInfoList) {
			((LogicInfoList)info).listChanged = true;
		}
	}

	public void onGridElementClicked(DisplayScreenClick click, LogicInfoList list, @Nullable MonitoredItemStack stack) {
		int networkID = (stack == null || stack.getNetworkSource() == -1) ? list.networkID.getObject() : stack.getNetworkSource();
		GSIClickPacketHelper.sendGSIClickPacket(GSIClickPacketHelper.createItemClickPacket(stack == null ? null : stack.getStoredStack(), networkID), getHolder().getContainer(), click);
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return IGuiOrigin.withOrigin(new GuiEditNetworkItemlist(this, obj), origin);
	}

	public static final String REGISTRY_NAME = "n_item_l";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
