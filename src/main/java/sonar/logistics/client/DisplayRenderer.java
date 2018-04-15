package sonar.logistics.client;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.translate;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.InfoRenderer;

//TWEAKED FAST MSR
public class DisplayRenderer extends TileEntitySpecialRenderer<TileAbstractDisplay> {

	public ResourceLocation hologram = new ResourceLocation(RenderHammer.modelFolder + "hologram.png");

	@Override
	public void render(TileAbstractDisplay part, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (part.getWorld() == null) {
			return;
		}
		if (part instanceof ILargeDisplay && (!((ILargeDisplay) part).shouldRender() || ((ILargeDisplay) part).getConnectedDisplay() == null || !((ILargeDisplay) part).getConnectedDisplay().canBeRendered.getObject())) {
			boolean bool = ((ILargeDisplay) part).getConnectedDisplay() == null ? false : !((ILargeDisplay) part).getConnectedDisplay().canBeRendered.getObject();

			if (bool) {
				DisplayGSI container = part.getGSI();
				RenderHelper.offsetRendering(part.getPos(), partialTicks);
				InfoRenderer.rotateDisplayRendering(container.getFacing(), container.getRotation(), 0, 0);

				translate(-1, -1, -0.01);
				// InfoRenderer.renderNormalInfo(part.getDisplayType().width, part.getDisplayType().height / 2, part.getDisplayType().scale, InfoError.incompleteDisplay.error);
				popMatrix();
			}

			return;
		}
		DisplayGSI container = part.getGSI();

		RenderHelper.offsetRendering(part.getPos(), partialTicks);
		InfoRenderer.rotateDisplayRendering(container.getFacing(), container.getRotation(), 0, 0);
		translate(-1, -1, -0.005);
		translate(part.getDisplayType().xPos, part.getDisplayType().yPos, 0);
		container.render();
		popMatrix();
		// }
	}
}
