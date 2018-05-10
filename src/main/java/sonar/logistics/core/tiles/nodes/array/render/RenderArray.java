package sonar.logistics.core.tiles.nodes.array.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.core.tiles.displays.tiles.DisplayVectorHelper;
import sonar.logistics.core.tiles.nodes.array.TileArray;

import static net.minecraft.client.renderer.GlStateManager.*;

public class RenderArray extends TileEntitySpecialRenderer<TileArray> {

	@Override
	public void render(TileArray te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		RenderHelper.offsetRendering(te.getPos(), partialTicks);
		Vec3d tVec = DisplayVectorHelper.getFaceOffset(te.getCableFace(), 0.5).addVector(0.5,0.5,0.5);
		Vec3d rVec = DisplayVectorHelper.getScreenRotation(te.getCableFace());
		//InfoRenderHelper.rotateDisplayRendering(te.getCableFace(), EnumFacing.NORTH, 0, 0);
		//translate(-(0.0625*10) - 0.01, -0.0625*11, 0);
		translate(tVec.x, tVec.y, tVec.z);
		GlStateManager.rotate(-(float)rVec.y, 0, 1, 0);
		GlStateManager.rotate((float)rVec.x, 1, 0, 0);
		GlStateManager.rotate((float)rVec.z, 0, 0, 1);
		rotate(90, -1, 0, 0);
		//translate(-9, -8.0, 0.45);
		for (int i = 0; i < te.inventory.getSizeInventory(); i++) {
			ItemStack stack = te.inventory.getStackInSlot(i).copy();
			if (!stack.isEmpty()) {
				pushMatrix();
				if (i < 4) {
					translate(-0.0625*2, 0, (i * 0.0625*2) - 0.0625*3);
				} else{
					translate(0.0625*2, 0, ((i - 4) * 0.0625*2) - 0.0625*3);
				}
				GlStateManager.depthMask(true);
				scale(0.0625*5, 0.0625*5, 0.0625*5);
				RenderHelper.itemRender.renderItem(stack, TransformType.NONE);
				popMatrix();
			}
		}
		popMatrix();
	}

}
