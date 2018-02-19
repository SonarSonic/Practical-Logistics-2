package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.*;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.MonitoredItemStack;

@DisplayElementType(id = NetworkItemElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkItemElement extends AbstractInfoElement<MonitoredItemStack> {

	public NetworkItemElement() {}
	
	public NetworkItemElement(InfoUUID uuid) {
		super(uuid);
	}

	public void render(MonitoredItemStack info) {
		
		disableLighting();
		scale(1, 1 , 0.1); //compresses the item on the z axis
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
