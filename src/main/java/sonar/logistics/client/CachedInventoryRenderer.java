package sonar.logistics.client;

import static net.minecraft.client.renderer.GlStateManager.alphaFunc;
import static net.minecraft.client.renderer.GlStateManager.blendFunc;
import static net.minecraft.client.renderer.GlStateManager.color;
import static net.minecraft.client.renderer.GlStateManager.depthMask;
import static net.minecraft.client.renderer.GlStateManager.disableAlpha;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;
import static net.minecraft.client.renderer.GlStateManager.disableRescaleNormal;
import static net.minecraft.client.renderer.GlStateManager.enableAlpha;
import static net.minecraft.client.renderer.GlStateManager.enableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableRescaleNormal;
import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.rotate;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.BlockModelsCache;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.lists.types.ItemChangeableList;
import sonar.logistics.api.lists.values.ItemCount;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.LogicInfoList;

/**W.I.P not fully implemented yet as I don't know how much of a performance increase it will offer. In fact it may be worse.*/
public class CachedInventoryRenderer {

	public List<CachedItemRenderer> renders = Lists.newArrayList();

	public void update(LogicInfoList info, ItemChangeableList list) {
		renders = create(info, list);
	}

	public List<CachedItemRenderer> create(LogicInfoList info, ItemChangeableList list) {
		List<CachedItemRenderer> renders = new ArrayList();
		int currentX = 0, currentY = 0;
		int pageCount = info.pageCount;
		for (int i = info.perPage * pageCount; i < Math.min(info.perPage + info.perPage * pageCount, list.getList().size()); i++) {
			ItemCount m = (ItemCount) list.values.get(i);
			StoredItemStack s = m.item.getStoredStack();
			int current = i - info.perPage * pageCount;
			int xLevel = (int) (current - ((Math.floor((current / info.xSlots))) * info.xSlots));
			int yLevel = (int) (Math.floor((current / info.xSlots)));
			int x = xLevel - currentX;
			int y = yLevel - currentY;
			currentX = xLevel;
			currentY = yLevel;
			renders.add(new CachedItemRenderer(s.item, BlockModelsCache.INSTANCE.getOrLoadModel(s.item), "" + s.getStackSize(), x, y));
		}
		return renders;
	}

	public void updateSizing(LogicInfoList info) {
		int currentX = 0, currentY = 0;

		for (int i = info.perPage * info.pageCount; i < Math.min(info.perPage + info.perPage * info.pageCount, renders.size()); i++) {
			CachedItemRenderer m = renders.get(i);
			int current = i - info.perPage * info.pageCount;
			int xLevel = (int) (current - ((Math.floor((current / info.xSlots))) * info.xSlots));
			int yLevel = (int) (Math.floor((current / info.xSlots)));
			int x = xLevel - currentX;
			int y = yLevel - currentY;
			currentX = xLevel;
			currentY = yLevel;
			m.x = x;
			m.y = y;
		}
	}

	public void render() {
		pushMatrix();
		RenderHelper.saveBlendState();
		color(1.0F, 1.0F, 1.0F, 1.0F);
		translate(-1 + (0.0625 * 1.3), -1 + 0.0625 * 5, 0.00);
		rotate(180, 0, 1, 0);
		scale(-0.022, 0.022, 0.01);
		RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		enableRescaleNormal();
		enableAlpha();
		alphaFunc(516, 0.1F);
		enableBlend();
		pushMatrix();
		for (CachedItemRenderer render : renders) {
			GL11.glTranslated(render.x, render.y, 0);
			pushMatrix();
			scale(1, 1, 0.04);
			disableLighting();
			renderItemModelIntoGUI(render.stack, 0, 0, render.model);
			popMatrix();
		}
		popMatrix();
		disableAlpha();
		disableRescaleNormal();
		disableLighting();
		disableBlend();
		RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

		translate(0, 0, 1);
		depthMask(false);
		pushMatrix();
		final float scaleFactor = 0.5F;
		final float inverseScaleFactor = 1.0f / scaleFactor;
		scale(scaleFactor, scaleFactor, scaleFactor);
		for (CachedItemRenderer render : renders) {
			translate(render.x * inverseScaleFactor, render.y * inverseScaleFactor, 0);
			final int X = (int) (((float) 0 + 15.0f - RenderHelper.fontRenderer.getStringWidth(render.stored) * scaleFactor) * inverseScaleFactor);
			final int Y = (int) (((float) 0 + 15.0f - 7.0f * scaleFactor) * inverseScaleFactor);
			RenderHelper.fontRenderer.drawStringWithShadow(render.stored, X, Y, 16777215);
		}

		popMatrix();
		depthMask(true);
		RenderHelper.restoreBlendState();
		popMatrix();
	}

	public void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel) {
		RenderHelper.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.setupGuiTransform(x, y, bakedmodel.isGui3d());
		bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
		RenderHelper.itemRender.renderItem(stack, bakedmodel);
	}

	public static class CachedItemRenderer {
		public double x, y; // how much they moved compared to the last one in the list
		public String stored;
		public ItemStack stack;
		public IBakedModel model;

		public CachedItemRenderer(ItemStack stack, IBakedModel model, String stored, int x, int y) {
			this.stack = stack;
			this.model = model;
			this.stored = stored;
			this.x = x * InfoRenderer.ITEM_SPACING;
			this.y = y * InfoRenderer.ITEM_SPACING;
		}

	}

}
