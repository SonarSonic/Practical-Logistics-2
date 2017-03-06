package sonar.logistics.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.Logistics;
import sonar.logistics.common.tileentity.TileEntityHammer;

public class RenderHammer extends TileEntitySpecialRenderer {

	public final static String modelFolder = Logistics.MODID + ":textures/model/";
	public ModelHammer model = new ModelHammer();
	public String texture = modelFolder + "hammer.png";
	public String textureNew = modelFolder + "hammer_machine.png";
	public ResourceLocation rope = new ResourceLocation(modelFolder + "rope.png");

	@Override
	public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float f, int par) {
		RenderHelper.beginRender(x + 0.5F, y + 1.5F, z + 0.5F, RenderHelper.setMetaData(entity), textureNew);
		int progress = 0;
		boolean cooling = false;
		if (entity != null && entity.getWorld() != null) {
			TileEntityHammer hammer = (TileEntityHammer) entity;
			if (hammer.coolDown.getObject() != 0) {
				progress = hammer.coolDown.getObject();
				cooling = true;
			} else
				progress = hammer.progress.getObject();
			// double move = progress * 1.625 / hammer.speed;
			double move = !cooling ? progress * 1.625 / hammer.speed : progress * 1.625 / 200;
			model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F, true, move);
		} else {
			model.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F, false, 0);
		}
		RenderHelper.finishRender();
		/* FIX IF YOU KNOW HOW - THANK YOU!
		if (entity.getWorld() != null) {	
			GL11.glPushMatrix();
			//GL11.glTranslated(x, y, z);		
			GL11.glTranslated(0, 2.75, 0);
			double height = -(!cooling ? progress * 1.625 / 100 : progress * 1.625 / 200);
			float width = 0.53F;
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();
			this.bindTexture(rope);
	        //GlStateManager.glTexParameteri(3553, 10242, 10497);
	        //GlStateManager.glTexParameteri(3553, 10243, 10497);
			//GL11.glTranslated(0.0, 0.70, 0.0);
			float f2 = 20;
			float f4 = -f2 * 0.2F - (float) MathHelper.floor_float(-f2 * 0.1F);
			byte b0 = 1;
			double d3 = (double) f2 * 0.025D * (1.0D - (double) (b0 & 1) * 2.5D);
			//GL11.glTranslated(0.0, -0.70, 0.0);
			//OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			//GL11.glDepthMask(true);
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			double remain = 1 - width;
			double offset = 0.2D - 1 / 4;
			double d18 = height;
			double d20 = 0.0D;
			double d22 = 1.0D;
			double d24 = (double) (-1.0F + f4) *50;
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
	       // GlStateManager.enableTexture2D();
			GL11.glDepthMask(true);
			GL11.glTranslated(0, -2.75, 0);
			GL11.glPopMatrix();		
		}
		 */
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		if (entity != null && entity.getWorld() != null && entity instanceof TileEntityHammer) {
			TileEntityHammer hammer = (TileEntityHammer) entity;
			ItemStack target = (progress == 0 || cooling) && hammer.getStackInSlot(1) != null ? hammer.getStackInSlot(1) : hammer.getStackInSlot(0);
			if (target != null) {
				if (!Minecraft.getMinecraft().getRenderItem().shouldRenderItemIn3D(target)) {
					GL11.glRotated(90, 1, 0, 0);
					GL11.glTranslated(0.5, 0.5 - 0.0625 * 2, -0.89);
				} else {
					GL11.glRotated(180, 1, 0, 0);
					GL11.glRotated(180, 0, 1, 0);
					GL11.glRotated(180, 0, 0, 1);
					GL11.glScaled(1.5, 1.5, 1.5);
					GL11.glTranslated(0.5 * 1 / 1.5, 0.52, 0.5 * 1 / 1.5);
					int pos = 81;
					int offset = 0;
					if (!cooling && progress > pos || cooling && (progress / 2) - offset > pos) {
						if (cooling) {
							progress = (progress / 2) - offset;
						}
						GL11.glTranslated(0, -((progress - pos) * 0.0015 / (hammer.speed - pos)), 0);
						GL11.glScaled(1, 1 - ((progress - pos) * 0.8 / (hammer.speed - pos)), 1);
					}
				}
				Minecraft.getMinecraft().getRenderItem().renderItem(target, TransformType.GROUND);
			}
		}
		GL11.glPopMatrix();
	}
}