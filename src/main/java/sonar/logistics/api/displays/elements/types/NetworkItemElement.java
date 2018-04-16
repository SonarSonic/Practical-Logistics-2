package sonar.logistics.api.displays.elements.types;

import static net.minecraft.client.renderer.GlStateManager.depthMask;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;
import static net.minecraft.client.renderer.GlStateManager.enableLighting;
import static net.minecraft.client.renderer.GlStateManager.rotate;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractInfoElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.info.types.MonitoredItemStack;

@DisplayElementType(id = NetworkItemElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkItemElement extends AbstractInfoElement<MonitoredItemStack> {

	public NetworkItemElement() {
		super();
	}

	public NetworkItemElement(InfoUUID uuid) {
		super(uuid);
	}

	public void render(MonitoredItemStack info) {
		disableLighting();
		scale(1, 1, 0.1); // compresses the item on the z axis
		rotate(180, 0, 1, 0); // flips the item
		scale(-1, 1, 1);
		RenderHelper.renderItemIntoGUI(info.getItemStack(), 0, 0);
		translate(0, 0, 2);
		depthMask(false);
		RenderHelper.renderStoredItemStackOverlay(info.getItemStack(), 0, 0, 0, "" + info.getStored(), false);
		depthMask(true);
		enableLighting();
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		// FIXME
		return null;
	}

	@Override
	public boolean isType(IInfo info) {
		return info instanceof MonitoredItemStack;
	}

	@Override
	public int[] createUnscaledWidthHeight() {
		return new int[] { 16, 16 };
	}

	public static final String REGISTRY_NAME = "n_item";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
