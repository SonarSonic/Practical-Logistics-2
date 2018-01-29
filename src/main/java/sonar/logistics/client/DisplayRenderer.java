package sonar.logistics.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.displays.TileHolographicDisplay;
import sonar.logistics.helpers.InfoRenderer;
import static net.minecraft.client.renderer.GlStateManager.*;

//TWEAKED FAST MSR
public class DisplayRenderer extends TileEntitySpecialRenderer<TileAbstractDisplay> {

	public ResourceLocation hologram = new ResourceLocation(RenderHammer.modelFolder + "hologram.png");

	@Override
	public void render(TileAbstractDisplay part, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (part instanceof ILargeDisplay && !((ILargeDisplay) part).shouldRender()) {
			return;
		}
		if (part.getDisplayType() != DisplayType.HOLOGRAPHIC) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			EnumFacing face = player.getHorizontalFacing().getOpposite();
			if (face == part.getCableFace().getOpposite()) {
				return;
			}
		}
		//GL11.glFlush();
		RenderHelper.offsetRendering(part.getPos(), partialTicks);

		if (part instanceof ILargeDisplay) {
			ConnectedDisplay screen = ((ILargeDisplay) part).getDisplayScreen();
			InfoRenderer.rotateDisplayRendering(part.getCableFace(), part.rotation, screen.width.getObject(), screen.height.getObject());
		} else {
			InfoRenderer.rotateDisplayRendering(part.getCableFace(), part.rotation, 0, 0);
		}

		if (part instanceof TileHolographicDisplay) {
			this.bindTexture(new ResourceLocation(RenderHammer.modelFolder + "hologram.png"));
			pushMatrix();
			RenderHelper.saveBlendState();
			pushAttrib();
			disableCull();
			depthMask(false);
			color(1.0F, 1.0F, 1.0F, 0.2F);
			translate(-1 - 0.042, -1.2, .01);
			scale(0.015, 0.015, 0.015);
			enableBlend();
			OpenGlHelper.glBlendFunc(770, 1, 1, 0);
			RenderHelper.drawScaledCustomSizeModalRect(0, 0, 0, 0, 64, 36, 64, 36, 64, 36);
			disableBlend();
			popAttrib();
			RenderHelper.restoreBlendState();
			popMatrix();
			color(1.0F, 1.0F, 1.0F, 1.0F);
			translate(-0.0625, 0, 0);
		}
		
		translate(-0.0625, 0, 0);
		part.container().renderContainer();

		//depthMask(true);
		popMatrix();

		//GL11.glFinish();
	}
}
