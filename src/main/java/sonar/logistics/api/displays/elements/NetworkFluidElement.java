package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.scale;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.ElementFillType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;

@DisplayElementType(id = NetworkFluidElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkFluidElement extends AbstractInfoElement<MonitoredFluidStack> {

	public NetworkFluidElement(InfoUUID uuid) {
		super(uuid);
	}

	public void render(MonitoredFluidStack info) {
		GL11.glPushMatrix();
		GlStateManager.disableLighting();
		GL11.glTranslated(0, 0, 0.001);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(info.getFluidStack().getFluid().getStill().toString());
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		InfoRenderer.renderProgressBarWithSprite(sprite, getActualScaling()[WIDTH], getActualScaling()[HEIGHT], info.fluidStack.getObject().stored, info.fluidStack.getObject().capacity);
		GlStateManager.enableLighting();
		GL11.glPopMatrix();
	}

	@Override
	public boolean isType(IInfo info) {
		return info instanceof MonitoredFluidStack;
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_SCALED_CONTAINER;
	}

	public static final String REGISTRY_NAME = "n_fluid";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
