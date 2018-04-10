package sonar.logistics.guide;

import static net.minecraft.client.renderer.GlStateManager.translate;

import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import sonar.core.client.gui.GuiBlockRenderer3D;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.helpers.InfoRenderer;

public class Logistics3DRenderer extends GuiBlockRenderer3D {

	public Logistics3DRenderer(int cubeSize) {
		super(cubeSize);
	}

	/* public void doMultipartRenderPass(Vector3d trans) { BufferBuilder wr = Tessellator.getInstance().getBuffer(); wr.begin(7, DefaultVertexFormats.BLOCK); Tessellator.getInstance().getBuffer().setTranslation(trans.x, trans.y, trans.z); for (Entry<BlockPos, List<MultipartStateOverride>> entry : multiparts.entrySet()) { BlockPos pos = entry.getKey(); for (MultipartStateOverride part : entry.getValue()) { IBlockState state = part.getActualState(MultipartRegistry.getDefaultState(part.part).getBaseState(), this, pos); renderMultipart(state, pos, this, Tessellator.getInstance().getBuffer()); } } Tessellator.getInstance().draw(); Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0); for (Entry<BlockPos, List<MultipartStateOverride>> entry : multiparts.entrySet()) { BlockPos pos = entry.getKey(); for (MultipartStateOverride part : entry.getValue()) { if (part.part instanceof AbstractDisplayPart) { try { renderScreenAt((AbstractDisplayPart) part.part, pos, 0); } catch (Throwable t) { } } } } mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE); } */
	public void doSpecialRender(GuiBlockRenderCache cache, Vector3d at) {
		if (cache.tile instanceof TileAbstractDisplay) {
			renderScreenAt(cache, cache.pos, 0);
		} else {
			super.doSpecialRender(cache, at);
		}
	}

	public void renderScreenAt(GuiBlockRenderCache cache, BlockPos pos, float partialTicks) {
		TileAbstractDisplay part = (TileAbstractDisplay) cache.tile;
		if (part instanceof TileLargeDisplayScreen && !((TileLargeDisplayScreen) part).shouldRender.getObject()) {
			return;
		}

		InfoContainer container = part.getGSI();
		GlStateManager.pushMatrix();
		translate(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5);

		if (part instanceof ILargeDisplay) {
			ConnectedDisplay screen = ((ILargeDisplay) part).getConnectedDisplay();
			if (screen == null)
				return;
			InfoRenderer.rotateDisplayRendering(cache.state.getValue(SonarProperties.ORIENTATION), container.getRotation(), screen.width.getObject(), screen.height.getObject());

		} else {
			InfoRenderer.rotateDisplayRendering(cache.state.getValue(SonarProperties.ORIENTATION), container.getRotation(), 0, 0);
		}

		GL11.glTranslated(-0.0625, 0, 0);
		if (part.getDisplayType() == DisplayType.LARGE) {
			GL11.glTranslated(0, -0.0625 * 4, 0);
		}
		DisplayLayout layout = container.getLayout();
		DisplayType type = part.getDisplayType();
		for (int dataPos = 0; dataPos < layout.maxInfo; dataPos++) {
			DisplayInfo info = part.getGSI().getDisplayInfo(dataPos);

			GL11.glPushMatrix();
			GlStateManager.pushAttrib();
			double[] translation = info.getRenderProperties().translation;
			double[] scaling = info.getRenderProperties().scaling;
			GL11.glTranslated(translation[0], translation[1], translation[2]);
			info.getGSI().renderGSIBackground(info.cachedInfo, (InfoContainer) part.getGSI(), info, scaling[0], scaling[1], scaling[2], dataPos);
			info.getGSI().renderGSIForeground(info.cachedInfo, (InfoContainer) part.getGSI(), info, scaling[0], scaling[1], scaling[2], dataPos);
			GlStateManager.popAttrib();
			GL11.glPopMatrix();
		}
		GlStateManager.popMatrix();
	}

}
