package sonar.logistics.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.common.multiparts.misc.TileClock;

public class ClockRenderer extends TileEntitySpecialRenderer<TileClock> {

	@Override
	public void render(TileClock te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		World world = te.getWorld();
		if (world != null) {
			GlStateManager.pushAttrib();
			BlockPos pos = te.getPos();
			RenderHelper.offsetRendering(pos, partialTicks);
			GlStateManager.disableLighting();
			GlStateManager.enableTexture2D();
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);
			BufferBuilder wr = Tessellator.getInstance().getBuffer();
			switch (te.getCableFace()) {
			case UP:
			case DOWN:
				GlStateManager.translate(0.5, 0, 0.5);
				wr.setTranslation(-pos.getX() - 0.5, -pos.getY(), -pos.getZ() - 0.5);
				GlStateManager.rotate(te.rotation, 0, 1, 0);
				break;
			case WEST:
			case EAST:
				GlStateManager.translate(0, 0.5, +0.5);
				wr.setTranslation(-pos.getX(), -pos.getY() - 0.5, -pos.getZ() - 0.5);
				GlStateManager.rotate(te.rotation, 1, 0, 0);
				break;
			case NORTH:
			case SOUTH:
				GlStateManager.translate(0.5, 0.5, 0);
				wr.setTranslation(-pos.getX() - 0.5, -pos.getY() - 0.5, -pos.getZ());
				GlStateManager.rotate(te.rotation, 0, 0, 1);
				break;
			default:
				break;

			}
			wr.begin(7, DefaultVertexFormats.BLOCK);
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			IBlockState state = te.getBlockType().getActualState(te.getBlockType().getDefaultState(), te.getWorld(), pos).withProperty(PL2Properties.CLOCK_HAND, true);
			EnumBlockRenderType type = state.getRenderType();
			if (type != EnumBlockRenderType.MODEL) {
				blockrendererdispatcher.renderBlock(state, pos, world, wr);
				return;
			}

			IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(state);
			blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, state, pos, wr, true);

			Tessellator.getInstance().draw();
			Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
			// GL11.glPopMatrix();
			GL11.glPopMatrix();
			GlStateManager.popAttrib();

		}
	}

}
