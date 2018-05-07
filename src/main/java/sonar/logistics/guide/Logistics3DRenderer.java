package sonar.logistics.guide;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sonar.core.client.gui.GuiBlockRenderer3D;
import sonar.logistics.api.displays.elements.IDisplayRenderable;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.GlStateManager.*;

public class Logistics3DRenderer extends GuiBlockRenderer3D {

	Map<BlockPos, List<DisplayElementContainer>> containers = new HashMap<>();

	public Logistics3DRenderer(int cubeSize) {
		super(cubeSize);
	}

	public void addDisplayContainer(BlockPos pos, DisplayElementContainer container) {
		containers.putIfAbsent(pos, new ArrayList<>());
		containers.get(pos).add(container);
	}

	public void doSpecialRender(GuiBlockRenderCache cache, Vector3d at) {
		if (cache.tile instanceof TileAbstractDisplay) {
			renderScreenAt(cache, at, cache.pos, 0);
		} else {
			super.doSpecialRender(cache, at);
		}
	}

	public void renderScreenAt(GuiBlockRenderCache cache, Vector3d at, BlockPos pos, float partialTicks) {
		TileAbstractDisplay part = (TileAbstractDisplay) cache.tile;
		if (part instanceof TileLargeDisplayScreen && !((TileLargeDisplayScreen) part).shouldRender.getObject()) {
			return;
		}
		super.doSpecialRender(cache, at);
		List<DisplayElementContainer> toRender = containers.get(pos);
		if (toRender != null && !toRender.isEmpty()) {
			try {

				pushMatrix();
				//RenderHelper.offsetRendering(part.getPos(), partialTicks);
				translate(at.x, at.y, at.z);
				InfoRenderer.rotateDisplayRendering(EnumFacing.NORTH, EnumFacing.NORTH, 0, 0);
				translate(-0.5, -1, -0.505);
				translate(part.getDisplayType().xPos, part.getDisplayType().yPos, 0);
				
				for (DisplayElementContainer c : toRender) {
					pushMatrix();
					translate(c.getTranslation()[0], c.getTranslation()[1], c.getTranslation()[2]);
					c.elements.forEach(IDisplayRenderable::updateRender);
					DisplayElementHelper.align(c.getAlignmentTranslation());
					DisplayElementHelper.renderElementStorageHolder(c);
					popMatrix();
				}
				popMatrix();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

}
