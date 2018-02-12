package sonar.logistics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.IInfoContainer;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayConstants;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.displays.TileHolographicDisplay;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.InfoError;

import static net.minecraft.client.renderer.GlStateManager.*;

//TWEAKED FAST MSR
public class DisplayRenderer extends TileEntitySpecialRenderer<TileAbstractDisplay> {

	public ResourceLocation hologram = new ResourceLocation(RenderHammer.modelFolder + "hologram.png");

	@Override
	public void render(TileAbstractDisplay part, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		//if (part.defaultData.getObject()) { //stops it flickering no data before first packet has been received.
			DisplayGSI container = part.getGSI();

			if (part instanceof ILargeDisplay && (!((ILargeDisplay) part).shouldRender() || !((ILargeDisplay) part).getConnectedDisplay().canBeRendered.getObject())) {
				boolean bool = !((ILargeDisplay) part).getConnectedDisplay().canBeRendered.getObject();

				if (bool) {
					RenderHelper.offsetRendering(part.getPos(), partialTicks);
					InfoRenderer.rotateDisplayRendering(container.getFacing(), container.getRotation(), 0, 0);
					//translate(-0.0625, 0, 0);
					InfoRenderer.renderNormalInfo(part.getDisplayType().width, part.getDisplayType().height / 2, part.getDisplayType().scale, InfoError.incompleteDisplay.error);
					popMatrix();
				}

				return;
			}

			RenderHelper.offsetRendering(part.getPos(), partialTicks);
			InfoRenderer.rotateDisplayRendering(container.getFacing(), container.getRotation(), 0, 0);
			translate(-1, -1, -0.01);
			translate(0.0625, 0.0625, 0);//FIXME - OFFSET FOR BEZEL
			part.getGSI().render();
			popMatrix();
		//}
	}
}
