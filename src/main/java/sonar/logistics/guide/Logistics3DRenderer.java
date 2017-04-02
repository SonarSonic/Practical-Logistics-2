package sonar.logistics.guide;

import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;

import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import sonar.core.client.gui.GuiBlockRenderer3D;
import sonar.core.client.gui.MultipartStateOverride;
import sonar.logistics.api.displays.ConnectedDisplayScreen;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.DisplayType;
import sonar.logistics.api.displays.ILargeDisplay;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.ScreenLayout;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.common.multiparts.ScreenMultipart;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.InfoError;

public class Logistics3DRenderer extends GuiBlockRenderer3D {

	public Logistics3DRenderer(int cubeSize) {
		super(cubeSize);
	}

	public void doMultipartRenderPass(Vector3d trans) {

		VertexBuffer wr = Tessellator.getInstance().getBuffer();
		wr.begin(7, DefaultVertexFormats.BLOCK);

		Tessellator.getInstance().getBuffer().setTranslation(trans.x, trans.y, trans.z);

		for (Entry<BlockPos, List<MultipartStateOverride>> entry : multiparts.entrySet()) {
			BlockPos pos = entry.getKey();
			for (MultipartStateOverride part : entry.getValue()) {
				IBlockState state = part.getActualState(MultipartRegistry.getDefaultState(part.part).getBaseState(), this, pos);
				renderMultipart(state, pos, this, Tessellator.getInstance().getBuffer());

			}
		}

		Tessellator.getInstance().draw();
		Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);

		for (Entry<BlockPos, List<MultipartStateOverride>> entry : multiparts.entrySet()) {
			BlockPos pos = entry.getKey();
			for (MultipartStateOverride part : entry.getValue()) {
				if (part.part instanceof ScreenMultipart) {
					try {
						renderScreenAt((ScreenMultipart) part.part, pos, 0);
					} catch (Throwable t) {

					}
				}
			}
		}
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
	}

	public void renderScreenAt(ScreenMultipart part, BlockPos pos, float partialTicks) {
		if(part instanceof ILargeDisplay && !((ILargeDisplay) part).shouldRender()){
			return;
		}
		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		translate(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5);
		if (part instanceof ILargeDisplay) {
			ConnectedDisplayScreen screen = ((ILargeDisplay) part).getDisplayScreen();
			InfoRenderer.rotateDisplayRendering(part.face, EnumFacing.NORTH, screen.width.getObject(), screen.height.getObject());
		} else {
			InfoRenderer.rotateDisplayRendering(part.face, EnumFacing.NORTH, 0, 0);
		}
		GL11.glTranslated(-0.0625, 0, 0);
		// part.container().renderContainer();
		if (part.getDisplayType() == DisplayType.LARGE) {
			GL11.glTranslated(0, -0.0625 * 4, 0);
		}
		ScreenLayout layout = part.getLayout();
		DisplayType type = part.getDisplayType();
		for (int dataPos = 0; dataPos < layout.maxInfo; dataPos++) {
			DisplayInfo info = part.container().getDisplayInfo(dataPos);

			GL11.glPushMatrix();
			GlStateManager.pushAttrib();
			double[] translation = info.getRenderProperties().translation;
			double[] scaling = info.getRenderProperties().scaling;
			GL11.glTranslated(translation[0], translation[1], translation[2]);

			if (info.cachedInfo != null && !info.getUnformattedStrings().isEmpty()) {
				InfoRenderer.renderNormalInfo(type, scaling[0], scaling[1], scaling[2], info.getFormattedStrings());
			} else {
				IMonitorInfo toDisplay = info.cachedInfo == null ? InfoError.noData : info.cachedInfo;
				toDisplay.renderInfo((InfoContainer) part.container(), info, scaling[0], scaling[1], scaling[2], dataPos);
			}
			GlStateManager.popAttrib();
			GL11.glPopMatrix();
		}

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

}
