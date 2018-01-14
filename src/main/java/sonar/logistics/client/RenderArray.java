package sonar.logistics.client;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.helpers.InfoRenderer;

public class RenderArray extends TileEntitySpecialRenderer<TileArray> {

	@Override
	public void render(TileArray te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		RenderHelper.offsetRendering(te.getPos(), partialTicks);
		InfoRenderer.rotateDisplayRendering(te.getCableFace(), EnumFacing.NORTH, 0, 0);
		GL11.glRotated(90, 1, 0, 0);
		GL11.glScaled(0.7, 0.7, 0.7);
		GL11.glTranslated(-9, -8.0, 0.45);
		for (int i = 0; i < te.inventory.getSizeInventory(); i++) {
			ItemStack stack = te.inventory.getStackInSlot(i);
			if (stack != null) {
				GlStateManager.pushMatrix();
				if (i < 4) {
					GlStateManager.translate(0, 0, i * 0.18);
				} else
					GlStateManager.translate(0.36, 0, (i - 4) * 0.18);
				RenderHelper.renderItem(stack, TransformType.NONE);
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
	}

}
