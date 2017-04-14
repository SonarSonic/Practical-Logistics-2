package sonar.logistics.client;

import org.lwjgl.opengl.GL11;

import mcmultipart.client.multipart.MultipartSpecialRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.common.multiparts.AbstractDisplayPart;
import sonar.logistics.common.multiparts.displays.HolographicDisplayPart;
import sonar.logistics.helpers.InfoRenderer;

//TWEAKED FAST MSR
public class DisplayRenderer extends MultipartSpecialRenderer<AbstractDisplayPart> {

	public ResourceLocation hologram = new ResourceLocation(RenderHammer.modelFolder + "hologram.png");

	@Override
	public void renderMultipartAt(AbstractDisplayPart part, double x, double y, double z, float partialTicks, int destroyStage) {
		if (part instanceof ILargeDisplay && !((ILargeDisplay) part).shouldRender()) {
			return;
		}
		RenderHelper.offsetRendering(part.getPos(), partialTicks);

		if (part instanceof ILargeDisplay) {
			ConnectedDisplay screen = ((ILargeDisplay) part).getDisplayScreen();
			InfoRenderer.rotateDisplayRendering(part.face, part.rotation, screen.width.getObject(), screen.height.getObject());
		} else {
			InfoRenderer.rotateDisplayRendering(part.face, part.rotation, 0, 0);
		}

		if (part instanceof HolographicDisplayPart) {
			
			this.bindTexture(new ResourceLocation(RenderHammer.modelFolder + "hologram.png"));
			GL11.glPushMatrix();
			RenderHelper.saveBlendState();
			GlStateManager.pushAttrib();
			GlStateManager.disableCull();
			GlStateManager.depthMask(false);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.2F);
			GlStateManager.translate(-1 - 0.042, -1.2, .01);
			GlStateManager.scale(0.015, 0.015, 0.015);
			GlStateManager.enableBlend();
			OpenGlHelper.glBlendFunc(770, 1, 1, 0);
			RenderHelper.drawScaledCustomSizeModalRect(0, 0, 0, 0, 64, 36, 64, 36, 64, 36);
			GlStateManager.disableBlend();
			GlStateManager.popAttrib();
			RenderHelper.restoreBlendState();
			GL11.glPopMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.translate(-0.0625, 0, 0);
			

		}
		GlStateManager.translate(-0.0625, 0, 0);
		part.container().renderContainer();

		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();

	}
}
