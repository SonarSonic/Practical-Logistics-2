package sonar.logistics.core.tiles.displays.tiles.render;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.core.tiles.displays.tiles.ILargeDisplay;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.info.InfoRenderHelper;
import sonar.logistics.core.tiles.displays.info.types.InfoError;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;
import sonar.logistics.core.tiles.misc.hammer.render.RenderHammer;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.translate;

public class DisplayRenderer extends TileEntitySpecialRenderer<TileAbstractDisplay> {

	public ResourceLocation hologram = new ResourceLocation(RenderHammer.modelFolder + "hologram.png");

	@Override
	public void render(TileAbstractDisplay part, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		DisplayGSI container = part.getGSI();
		if(container == null){
			return;
		}
        if (part instanceof ILargeDisplay && (!((ILargeDisplay) part).shouldRender() || ((ILargeDisplay) part).getConnectedDisplay() == null || !((ILargeDisplay) part).getConnectedDisplay().canBeRendered.getObject())) {
			boolean bool = ((ILargeDisplay) part).getConnectedDisplay() != null && !((ILargeDisplay) part).getConnectedDisplay().canBeRendered.getObject();

			if (bool) {
				RenderHelper.offsetRendering(part.getPos(), partialTicks);
				InfoRenderHelper.rotateDisplayRendering(container.getFacing(), container.getRotation(), 0, 0);

				translate(-1, -1, -0.01);
				translate(part.getDisplayType().xPos, part.getDisplayType().yPos, 0);
				InfoRenderHelper.renderCenteredStringsWithAdaptiveScaling(part.getDisplayType().width, part.getDisplayType().height, 0.06, 0, 0.75, -1, Lists.newArrayList(InfoError.incompleteDisplay.error));
			
				popMatrix();
			}

			return;
		}

		RenderHelper.offsetRendering(part.getPos(), partialTicks);
		InfoRenderHelper.rotateDisplayRendering(container.getFacing(), container.getRotation(), 0, 0);
		translate(-1, -1, -0.005);
		translate(part.getDisplayType().xPos, part.getDisplayType().yPos, 0);
		container.render();
		popMatrix();
	}
}
