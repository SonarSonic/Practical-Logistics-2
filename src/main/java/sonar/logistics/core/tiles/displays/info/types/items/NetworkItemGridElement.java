package sonar.logistics.core.tiles.displays.info.types.items;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMDisplayElement;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIClickPacketHelper;
import sonar.logistics.core.tiles.displays.info.elements.NetworkGridElement;
import sonar.logistics.core.tiles.displays.info.types.LogicInfoList;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import javax.annotation.Nullable;

import static net.minecraft.client.renderer.GlStateManager.*;

@ASMDisplayElement(id = NetworkItemGridElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkItemGridElement extends NetworkGridElement<MonitoredItemStack> {

	public NetworkItemGridElement() {
		super();
	}

	public NetworkItemGridElement(InfoUUID uuid) {
		super(uuid);
	}

	public double getRenderWidth() {
		return Math.round(Math.min(element_size, Math.min(getActualScaling()[0], getActualScaling()[1])) * 10000d) / 10000d;
	}

	public double getRenderHeight() {
		return Math.round(Math.min(element_size, Math.min(getActualScaling()[0], getActualScaling()[1])) * 10000d) / 10000d;
	}

	public void renderGridElement(MonitoredItemStack stack, int index) {
		scale((width / 16) * grid_fill_percentage, (height / 16) * grid_fill_percentage, 0.02);
		disableLighting();
		RenderHelper.renderItemIntoGUI(stack.getItemStack(), 0, 0);
		translate(0, 0, 0.8);
		depthMask(false);
		RenderHelper.renderStoredItemStackOverlay(stack.getItemStack(), 0, 0, 0, text_colour, "" + stack.getStored(), false);
		GlStateManager.color(1, 1, 1, 1);
		depthMask(true);
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
