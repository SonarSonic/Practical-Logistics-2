package sonar.logistics.client;

import static net.minecraft.client.renderer.GlStateManager.color;
import static net.minecraft.client.renderer.GlStateManager.depthMask;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.disableCull;
import static net.minecraft.client.renderer.GlStateManager.enableBlend;
import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;
import static net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.common.multiparts.displays.TileHolographicDisplay;
import sonar.logistics.helpers.InfoRenderer;

public class HolographicDisplayRenderer extends TileEntitySpecialRenderer<TileHolographicDisplay> {

	public ResourceLocation hologram = new ResourceLocation(RenderHammer.modelFolder + "hologram.png");

	@Override
	public void render(TileHolographicDisplay part, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		DisplayGSI container = part.getGSI();
		EntityPlayer player = Minecraft.getMinecraft().player;
		EnumFacing face = player.getHorizontalFacing().getOpposite();
		if (face == container.getFacing().getOpposite()) {
			return;
		}

		RenderHelper.offsetRendering(part.getPos(), partialTicks);

		InfoRenderer.rotateDisplayRendering(container.getFacing(), container.getRotation(), 0, 0);

        bindTexture(new ResourceLocation(RenderHammer.modelFolder + "hologram.png"));
        pushMatrix();
        RenderHelper.saveBlendState();
        disableCull();
        depthMask(false);
        color(1.0F, 1.0F, 1.0F, 0.2F);
        translate(-1 - 0.042, -1.2, .01);
        scale(0.015, 0.015, 0.015);
        enableBlend();
        tryBlendFuncSeparate(770, 1, 1, 0);
        RenderHelper.drawScaledCustomSizeModalRect(0, 0, 0, 0, 64, 36, 64, 36, 64, 36);
        disableBlend();
        RenderHelper.restoreBlendState();
        popMatrix();
        color(1.0F, 1.0F, 1.0F, 1.0F);
        translate(-0.0625, 0, 0);

        translate(-0.0625, 0, 0);
		part.getGSI().render();
		popMatrix();
	}
}
