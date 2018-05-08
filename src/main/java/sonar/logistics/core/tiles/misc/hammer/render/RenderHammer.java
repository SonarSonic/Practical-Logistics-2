package sonar.logistics.core.tiles.misc.hammer.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.core.tiles.misc.hammer.TileEntityHammer;

import static net.minecraft.client.renderer.GlStateManager.*;

public class RenderHammer extends TileEntitySpecialRenderer<TileEntityHammer> {

	public final static String modelFolder = PL2Constants.MODID + ":textures/model/";
	public String texture = modelFolder + "forging_hammer_stone.png";
	public ModelHammer model = new ModelHammer();
	public ResourceLocation rope = new ResourceLocation(modelFolder + "rope.png");

	@Override
	public void render(TileEntityHammer hammer, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		RenderHelper.beginRender(x + 0.5F, y + 1.5F, z + 0.5F, RenderHelper.setMetaData(hammer), texture);
		int progress = 0;
		boolean cooling = false;
		if (hammer != null) {
			if (hammer.coolDown.getObject() != 0) {
				progress = hammer.coolDown.getObject();
				cooling = true;
			} else
				progress = hammer.progress.getObject();
			// double move = progress * 1.625 / hammer.speed;
			double move = !cooling ? progress * 1.625 / TileEntityHammer.speed : progress * 1.625 / 200;
			model.render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F, true, move);
		} else {
			model.render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F, false, 0);
		}
		RenderHelper.finishRender();
		/*
		if (hammer.getWorld() != null) {
			translate(0, 2.75, 0);
			double height = -(!cooling ? progress * 1.625 / 100 : progress * 1.625 / 200);
			// double height = -(progress * 1.625 / 100);
			float width = 0.53F;
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			this.bindTexture(rope);
			glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
			glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
			translate(0.0, 0.70, 0.0);
			float f2 = 20;
			float f4 = -f2 * 0.2F - (float) Math.floor((double)-f2 * 0.1F);
			byte b0 = 1;
			double d3 = (double) f2 * 0.025D * (1.0D - (double) (b0 & 1) * 2.5D);
			translate(0.0, -0.70, 0.0);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			//depthMask(true);
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);

			double remain = 1 - width;
			double offset = 0.2D - 1 / 4;
			double d18 = height;
			double d20 = 0.0D;
			double d22 = 1.0D;
			double d24 = (double) (-1.0F + f4);
			double d26 = d18 + d24;

			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y + d18, z + remain, d22, d26);
			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y, z + remain, d22, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y, z + remain, d20, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y + d18, z + remain, d20, d26);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y + d18, z + width, d22, d26);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y, z + width, d22, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y, z + width, d20, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y + d18, z + width, d20, d26);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y + d18, z + remain, d22, d26);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y, z + remain, d22, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y, z + width, d20, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + width, y + d18, z + width, d20, d26);
			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y + d18, z + width, d22, d26);
			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y, z + width, d22, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y, z + remain, d20, d24);
			RenderHelper.addVertexWithUV(vertexbuffer, x + remain, y + d18, z + remain, d20, d26);

			tessellator.draw();
			//depthMask(false);
			translate(0, -2.75, 0);
		}
		*/
		GL11.glPushMatrix();
		translate(x, y, z);

		if (hammer != null) {
			ItemStack target = (progress == 0 || cooling) && hammer.getStackInSlot(1) != null ? hammer.getStackInSlot(1) : hammer.getStackInSlot(0);
			if (target != null) {
				if (!Minecraft.getMinecraft().getRenderItem().shouldRenderItemIn3D(target)) {
					rotate(90, 1, 0, 0);
					translate(0.5, 0.5 - 0.0625 * 2, -0.89);
				} else {
					rotate(180, 1, 0, 0);
					rotate(180, 0, 1, 0);
					rotate(180, 0, 0, 1);
					scale(1.5, 1.5, 1.5);
					translate(0.5 * 1 / 1.5, 0.52, 0.5 * 1 / 1.5);
					int pos = 81;
					int offset = 0;
					if (!cooling && progress > pos || cooling && (progress / 2) - offset > pos) {
						if (cooling) {
							progress = (progress / 2) - offset;
						}
						translate(0, -((progress - pos) * 0.0015 / (TileEntityHammer.speed - pos)), 0);
						scale(1, 1 - ((progress - pos) * 0.8 / (TileEntityHammer.speed - pos)), 1);
					}
				}
				Minecraft.getMinecraft().getRenderItem().renderItem(target, TransformType.GROUND);
			}
		}
		GL11.glPopMatrix();
	}
}