package sonar.logistics.helpers;

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

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import sonar.core.client.BlockModelsCache;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.helpers.SonarHelper;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredItemStack;

public class InfoRenderer {

	public static final double zLevel = 0, barOffset = 0.001;

	public static void renderNormalInfo(DisplayType type, String... toDisplay) {
		renderNormalInfo(type, type.width, type.height, type.scale, SonarHelper.convertArray(toDisplay));
	}

	public static void renderNormalInfo(DisplayType type, List<String> toDisplay) {
		renderNormalInfo(type, type.width, type.height, type.scale, toDisplay);
	}

	public static double getYCentre(DisplayType type, double height) {
		return ((0.12 * height) * (0.12 * height)) + (0.35 * height) - 0.58; // quadratic equation to solve the scale
	}

	public static void renderNormalInfo(DisplayType displayType, double width, double height, double scale, String... toDisplay) {
		renderNormalInfo(displayType, width, height, scale, SonarHelper.convertArray(toDisplay));
	}

	public static void renderNormalInfo(DisplayType displayType, double width, double height, double scale, int colour, String... toDisplay) {
		renderNormalInfo(displayType, width, height, scale, colour, SonarHelper.convertArray(toDisplay));
	}

	public static void renderNormalInfo(DisplayType displayType, double width, double height, double scale, List<String> toDisplay) {
		renderNormalInfo(displayType, width, height, scale, -1, toDisplay);
	}

	public static void renderNormalInfo(DisplayType displayType, double width, double height, double scale, int colour, List<String> toDisplay) {
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		float offset = (float) (12 / (1 / scale));
		double yCentre = 0;
		double centre = (double) toDisplay.size() / 2 - 0.5;
		int fontHeight = RenderHelper.fontRenderer.FONT_HEIGHT;
		for (int i = 0; i < toDisplay.size(); i++) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, (-1 + height / 2 + 0.26) + (i == centre ? 0 : i < centre ? yCentre - offset * -(i - centre) : yCentre + offset * (i - centre)), 0);
			GlStateManager.scale(scale, scale, 1.0f);
			String string = toDisplay.get(i);
			int length = RenderHelper.fontRenderer.getStringWidth(string);
			RenderHelper.fontRenderer.drawString(string, (float) ((-1 + 0.0625 + width / 2) / scale - length / 2), (float) 0.625, colour, false);
			GlStateManager.popMatrix();
		}
		GlStateManager.disableCull();
		GlStateManager.enableLighting();

	}

	/** NEED TO WORK OUT A FORMULA! */
	public static double yCentreScale(double height) {
		/* int newHeight = (int) (height + 0.0625 * 2) - 1; switch (newHeight) { case 0: return -16; case 1: return 2.9375; case 2: return 9.4375; case 3: return 12.7375; case 4: return 14.6375; case 5: return 16.0375; case 6: return 16.9375; case 7: return 17.6375; case 8: return 18.1375; case 9: return 0; case 14: return 20; } return 0; */

		// y = -0.0023x^6 + 0.067x^5 - 0.774x^4 + 4.6276x^3 - 15.524x^2 + 30.49^5x - 15.993

		double y = -0.0023 * Math.pow(height, 6) + 0.067 * Math.pow(height, 5) - 0.774 * Math.pow(height, 4) + 4.6276 * Math.pow(height, 3) - 15.524 * Math.pow(height, 2) + 30.49 * (height) - 15.993;

		return y;
	}

	public static void renderProgressBar(double width, double height, double scale, double d, double e) {
		GlStateManager.depthMask(true);
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);

		double minX = -barOffset + 0.0625, minY = -barOffset + 0.0625 * 1, maxX = width + barOffset + 0.0625 * 1, maxY = height + barOffset + 0.0625 * 1;
		double barWidth = d * (maxX - minX) / e;
		double divide = Math.max((maxX - minX), (maxY - minY));
		double minU = 0, minV = 0, maxU = 1, maxV = 1;

		double widthnew = (minU + (barWidth * (maxU - minU) / 1));
		double heightnew = (minV + ((maxY - minY) * (maxV - minV) / 1));
		vertexbuffer.pos((double) (minX + 0), maxY, zLevel).tex((double) minU, heightnew).endVertex();
		vertexbuffer.pos((double) (minX + barWidth), maxY, zLevel).tex(widthnew, heightnew).endVertex();
		vertexbuffer.pos((double) (minX + barWidth), (double) (minY + 0), zLevel).tex(widthnew, (double) minV).endVertex();
		vertexbuffer.pos((double) (minX + 0), (double) (minY + 0), zLevel).tex((double) minU, (double) minV).endVertex();
		tessellator.draw();
	}

	public static void renderProgressBarWithSprite(TextureAtlasSprite sprite, double width, double height, double scale, double progress, double maxProgress) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);

		double minX = -barOffset + 0.0625, minY = -barOffset + 0.0625 * 1, maxX = width + barOffset + 0.0625 * 1, maxY = height + barOffset + 0.0625 * 1;
		double barWidth = ((double) progress * (maxX - minX)) / maxProgress;
		double divide = Math.max((maxX - minX), (maxY - minY));

		double widthnew = (sprite.getMinU() + (barWidth * (sprite.getMaxU() - sprite.getMinU()) / (maxX - minX)));
		double heightnew = (sprite.getMinV() + ((maxY - minY) * (sprite.getMaxV() - sprite.getMinV()) / (maxX - minX)));
		vertexbuffer.pos((double) (minX + 0), maxY, zLevel).tex((double) sprite.getMinU(), heightnew).endVertex();
		vertexbuffer.pos((double) (minX + barWidth), maxY, zLevel).tex(widthnew, heightnew).endVertex();
		vertexbuffer.pos((double) (minX + barWidth), (double) (minY + 0), zLevel).tex(widthnew, (double) sprite.getMinV()).endVertex();
		vertexbuffer.pos((double) (minX + 0), (double) (minY + 0), zLevel).tex((double) sprite.getMinU(), (double) sprite.getMinV()).endVertex();
		tessellator.draw();
	}

	public static final int[] rotate = new int[] { 0, 0, 0, 180, 270, 90 };
	public static final double[][] matrix = new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 }, { 1, 0, -1 }, { 1, 0, 0 }, { 0, 0, -1 } };
	public static final double[][] downMatrix = new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 1, 0 }, { 1, 0, 0 }, { 0, 0, 0 }, { 1, 1, 0 } };
	public static final double[][] upMatrix = new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, -1 }, { 1, 1, -1 }, { 1, 0, -1 }, { 0, 1, -1 } };

	public static void rotateDisplayRendering(EnumFacing face, EnumFacing rotation, int width, int height) {
		double[] translate = matrix[face.ordinal()];
		GL11.glRotated(180, 0, 0, 1);
		switch (face) {
		case DOWN:
			GL11.glRotated(90, 1, 0, 0);

			int ordinal = rotation.ordinal();
			ordinal = ordinal == 4 ? 5 : ordinal == 5 ? 4 : ordinal;
			GL11.glRotated(rotate[ordinal], 0, 0, 1);
			translate = getDownMatrix(ordinal, width, height);

			break;
		case UP:
			GL11.glRotated(270, 1, 0, 0);

			GL11.glRotated(rotate[rotation.ordinal()], 0, 0, 1);
			translate = getUpMatrix(rotation.ordinal(), width, height);
			GL11.glTranslated(0, 0, 0);
			break;
		default:
			GL11.glRotated(rotate[face.ordinal()], 0, 1, 0);
			break;

		}
		GL11.glTranslated(translate[0] + 0.0625, translate[1], translate[2] - 0.005);
	}

	public static double[] getDownMatrix(int i, int width, int height) {
		double[][] newMatrix = new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, width, 0 }, { height, 0, 0 }, { 0, 0, 0 }, { height, width, 0 } };
		return newMatrix[i];
	}

	public static double[] getUpMatrix(int i, int width, int height) {
		double[][] newMatrix = new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, -1 }, { 1, 1, -1 }, { 1, 0, -1 }, { 0, 1, -1 } };
		return newMatrix[i];
	}

	public static int identifierLeft = (int) ((1.0 / 0.75) * 10);
	public static int objectLeft = (int) ((1.0 / 0.75) * (10 + 92));
	public static int kindLeft = (int) ((1.0 / 0.75) * (10 + 92 + 92));

	public static void renderMonitorInfoInGUI(IInfo info, int yPos, int colour) {
		if (info instanceof INameableInfo) {
			INameableInfo directInfo = (INameableInfo) info;
			if (!directInfo.isHeader() && directInfo.isValid()) {
				FontHelper.text(directInfo.getClientIdentifier(), identifierLeft, yPos, colour);
				FontHelper.text(directInfo.getClientObject(), objectLeft, yPos, colour);
				FontHelper.text(directInfo.getClientType(), kindLeft, yPos, colour);
			} else {
				if (directInfo instanceof LogicInfo) {
					String category = ((LogicInfo) directInfo).getRegistryType().name();
					FontHelper.text(category.substring(0, 1) + category.substring(1).toLowerCase(), identifierLeft + 4, yPos, colour);
				}
			}
		} else if (info instanceof MonitoredBlockCoords) {
			MonitoredBlockCoords directInfo = (MonitoredBlockCoords) info;
			FontHelper.text(directInfo.syncCoords.toString(), identifierLeft, yPos, colour);
		}
	}

	public static final double ITEM_SPACING = 22.7;
	public static final double FLUID_DIMENSION = (14 * 0.0625);

	public static void renderInventory(MonitoredList<MonitoredItemStack> stacks, int start, int stop, int xSlots, int ySlots) {
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
		for (int i = start; i < stop; i++) {
			MonitoredItemStack stack = stacks.get(i);
			int current = i - start;
			int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
			int yLevel = (int) (Math.floor((current / xSlots)));
			pushMatrix();
			GL11.glTranslated(xLevel * ITEM_SPACING, yLevel * ITEM_SPACING, 0);
			scale(1, 1, 0.04);
			disableLighting();
			renderItemModelIntoGUI(stack.getItemStack(), 0, 0);
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
		for (int i = start; i < stop; i++) {
			MonitoredItemStack stack = stacks.get(i);
			int current = i - start;
			int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
			int yLevel = (int) (Math.floor((current / xSlots)));
			pushMatrix();
			translate((xLevel * ITEM_SPACING) * inverseScaleFactor, (yLevel * ITEM_SPACING) * inverseScaleFactor, 0);
			String s = "" + stack.getStored();
			final int X = (int) (((float) 0 + 15.0f - RenderHelper.fontRenderer.getStringWidth(s) * scaleFactor) * inverseScaleFactor);
			final int Y = (int) (((float) 0 + 15.0f - 7.0f * scaleFactor) * inverseScaleFactor);
			RenderHelper.fontRenderer.drawStringWithShadow(s, X, Y, 16777215);
			popMatrix();
		}

		popMatrix();
		depthMask(true);
		RenderHelper.restoreBlendState();
		popMatrix();
	}

	public static void renderItemModelIntoGUI(ItemStack stack, int x, int y) {
		renderItemModelIntoGUI(stack, x, y, BlockModelsCache.INSTANCE.getOrLoadModel(stack));
	}

	public static void renderItemModelIntoGUI(ItemStack stack, int x, int y, IBakedModel bakedmodel) {
		RenderHelper.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.setupGuiTransform(x, y, bakedmodel.isGui3d());
		bakedmodel = ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);
		RenderHelper.itemRender.renderItem(stack, bakedmodel);
	}
}
