package sonar.logistics.client.gsi.info;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidStack;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.MonitoredFluidStack;

public class GSIFluidStack extends AbstractGSI<MonitoredFluidStack> {

	@Override
	public void renderGSIForeground(MonitoredFluidStack info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		FluidStack stack = info.fluidStack.getObject().fluid;
		if (stack != null) {
			GL11.glPushMatrix();
			GL11.glPushMatrix();
			GlStateManager.disableLighting();
			GL11.glTranslated(-1, -0.0625 * 12, +0.004);
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(stack.getFluid().getStill().toString());
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			InfoRenderer.renderProgressBarWithSprite(sprite, width, height, info.fluidStack.getObject().stored, info.fluidStack.getObject().capacity);
			GlStateManager.enableLighting();
			GL11.glTranslated(0, 0, -0.001);
			GL11.glPopMatrix();
			InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, displayInfo.getFormattedStrings());
			GL11.glPopMatrix();
		}
	}

	@Override
	public void onGSIClicked(MonitoredFluidStack info, DisplayScreenClick click, EnumHand hand) {
		super.onGSIClicked(info, click, hand);
		NBTTagCompound packet = GSIHelper.createFluidClickPacket(info.getStoredStack(), info.getNetworkSource());
		sendGSIPacket(packet, info, click, hand);
	}

}
