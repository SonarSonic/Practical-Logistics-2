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

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
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
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.elements.HeightAlignment;
import sonar.logistics.api.displays.elements.WidthAlignment;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredItemStack;

public class InfoRenderer {

	public static final double zLevel = 0, barOffset = 0.001;

	@Deprecated
	public static void renderNormalInfo(DisplayType type, String... toDisplay) {
		renderNormalInfo(type.width, type.height, type.scale, SonarHelper.convertArray(toDisplay));
	}

	@Deprecated
	public static void renderNormalInfo(DisplayType type, List<String> toDisplay) {
		renderNormalInfo(type.width, type.height, type.scale, toDisplay);
	}

	@Deprecated
	public static void renderNormalInfo(double width, double height, double scale, String... toDisplay) {
		renderNormalInfo(width, height, scale, SonarHelper.convertArray(toDisplay));
	}

	@Deprecated
	public static void renderNormalInfo(double width, double height, double scale, int colour, String... toDisplay) {
		renderNormalInfo(width, height, scale, colour, SonarHelper.convertArray(toDisplay));
	}

	@Deprecated
	public static void renderNormalInfo(double width, double height, double scale, List<String> toDisplay) {
		renderNormalInfo(width, height, scale, -1, toDisplay);
	}

	@Deprecated
	public static void renderNormalInfo(double width, double height, double maxScale, int colour, List<String> toDisplay) {

		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		float offset = (float) (12 / (1 / maxScale));
		double yCentre = 0;
		double centre = (double) toDisplay.size() / 2 - 0.5;
		int fontHeight = RenderHelper.fontRenderer.FONT_HEIGHT;
		for (int i = 0; i < toDisplay.size(); i++) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, (-1 + height / 2 + 0.26) + (i == centre ? 0 : i < centre ? yCentre - offset * -(i - centre) : yCentre + offset * (i - centre)), 0);
			GlStateManager.scale(maxScale, maxScale, 1.0f);
			String string = toDisplay.get(i);
			int length = RenderHelper.fontRenderer.getStringWidth(string);
			RenderHelper.fontRenderer.drawString(string, (float) ((-1 + 0.0625 + width / 2) / maxScale - length / 2), (float) 0.625, colour, false);
			GlStateManager.popMatrix();
		}
		GlStateManager.disableCull();
		GlStateManager.enableLighting();

	}

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;

	/** @param actualScaling the actual scaling
	 * @param maxListScaling the max scaling //FIXME not necessary, should be scaled to fit given scaling regardless of max list scaling
	 * @param percentageFill
	 * @param elements */
	public static void renderDisplayElements(double[] actualListScaling, double[] maxListScaling, double percentageFill, List<IDisplayElement> elements) {
		for (IDisplayElement e : elements) {
			double[] maxElementScaling = e.getMaxScaling();
			double[] actualElementScaling = e.getActualScaling();
			int[] unscaledWidthHeight = e.getUnscaledWidthHeight();
			pushMatrix();
			align(new double[] { maxListScaling[0], maxElementScaling[1], maxElementScaling[2] }, actualElementScaling, e.getWidthAlignment(), e.getHeightAlignment());
			scale(actualElementScaling[SCALE], actualElementScaling[SCALE], actualElementScaling[SCALE]);
			pushMatrix();
			e.render();
			popMatrix();
			popMatrix();
			translate(0, actualElementScaling[HEIGHT], 0);
		}
	}

	public static void align(double[] actualListScaling, double[] actualElementScaling, WidthAlignment width, HeightAlignment height) {
		alignWidth(actualListScaling, actualElementScaling, width);
		alignHeight(actualListScaling, actualElementScaling, height);
	}

	public static void alignWidth(double[] actualListScaling, double[] actualElementScaling, WidthAlignment align) {
		switch (align) {
		case CENTERED:
			translate((actualListScaling[WIDTH] / 2) - (actualElementScaling[WIDTH] / 2), 0, 0);
			break;
		case LEFT:
			break;
		case RIGHT:
			translate(actualListScaling[WIDTH] - actualElementScaling[WIDTH], 0, 0);
			break;
		default:
			break;

		}
	}

	public static void alignHeight(double[] actualListScaling, double[] actualElementScaling, HeightAlignment align) {
		switch (align) {
		case CENTERED:
			translate(0, actualListScaling[HEIGHT] / 2 - actualElementScaling[HEIGHT] / 2, 0);
			break;
		case TOP:
			break;
		case BOTTOM:
			translate(0, actualListScaling[HEIGHT] - actualElementScaling[HEIGHT], 0);
			break;
		default:
			break;

		}
	}

	/** scales the unscaled width and height to match the given scaling returned in the form, actual width, actual height, scale factor */
	public static double[] getScaling(int[] unscaled, double[] scaling, double percentageFill) {
		double actualElementScale = Math.min(scaling[0] / unscaled[0], scaling[1] / unscaled[1]);
		double actualElementWidth = (unscaled[0] * actualElementScale) * percentageFill;
		double actualElementHeight = (unscaled[1] * actualElementScale) * percentageFill;
		return new double[] { actualElementWidth, actualElementHeight, actualElementScale };
	}

	public static void renderCenteredStringsWithAdaptiveScaling(double width, double height, double maxScale, int spacing, double percentageFill, int colour, List<String> toDisplay) {
		double maxIndividualHeight = height / toDisplay.size();

		double compressedHeight = 0;
		double maximumWidth = 0;
		int unscaledMaximumWidth = 0;
		List<Double[]> matrices = Lists.newArrayList();

		for (int i = 0; i < toDisplay.size(); i++) {
			String s = toDisplay.get(i);
			int unscaledWidth = getStringWidth(s);
			int unscaledHeight = getStringHeight();
			double scale = Math.min(width / unscaledWidth, maxIndividualHeight / unscaledHeight);
			double maxWidth = (unscaledWidth * scale) * percentageFill;
			double maxHeight = (unscaledHeight * scale) * percentageFill;
			matrices.add(new Double[] { maxWidth, maxHeight, scale });
			if (maxWidth > maximumWidth) {
				maximumWidth = maxWidth;
				unscaledMaximumWidth = unscaledWidth;
			}
			compressedHeight += maxHeight;

		}
		GlStateManager.translate((width - maximumWidth) / 2, (height - compressedHeight) / 2, 0);
		for (int i = 0; i < toDisplay.size(); i++) {
			String s = toDisplay.get(i);
			Double[] matrix = matrices.get(i);

			// if (i != 0) GlStateManager.translate(0, matrix[1], 0); //offset

			//// START LIFT \\\\
			GlStateManager.pushMatrix();
			int unscaledWidth = getStringWidth(s);
			int unscaledHeight = getStringHeight();
			double scale = Math.min(width / unscaledWidth, matrix[1] / unscaledHeight);
			double maxWidth = (unscaledWidth * scale);
			double maxHeight = (unscaledHeight * scale);
			GlStateManager.translate((maximumWidth - maxWidth) / 2, 0, 0);
			GlStateManager.scale(scale, scale, scale);

			//// START STRING RENDERING \\\\

			GlStateManager.pushMatrix();
			RenderHelper.fontRenderer.drawString(s, 0, 0, colour);
			// GlStateManager.translate(-((maximumWidth / 2) - (maxWidth / 2)), 0, 0);
			GlStateManager.popMatrix();
			//// STOP STRING RENDERING \\\\

			GlStateManager.popMatrix();
			//// END LIFT \\\\

			GlStateManager.translate(0, maxHeight, 0);
		}
	}

	public static void renderCenteredStringsWithUniformScaling(double width, double height, double maxScale, int spacing, double percentageFill, int colour, List<String> toDisplay) {
		GlStateManager.pushMatrix();
		int unscaledWidth = getMaxWidth(toDisplay);
		int unscaledHeight = getMaxHeight(toDisplay, spacing);
		double scale = Math.min(width / unscaledWidth, height / unscaledHeight);
		double maxWidth = (unscaledWidth * scale) * percentageFill;
		double maxHeight = (unscaledHeight * scale) * percentageFill;
		GlStateManager.translate((width - maxWidth) / 2, (height - maxHeight) / 2, 0);
		GlStateManager.scale(scale, scale, scale);
		for (int i = 0; i < toDisplay.size(); i++) {
			if (i != 0)
				GlStateManager.translate(0, spacing + getStringHeight(), 0);
			String s = toDisplay.get(i);
			GlStateManager.pushMatrix();
			GlStateManager.translate(((unscaledWidth / 2) - (getStringWidth(s) / 2)), 0, 0);
			RenderHelper.fontRenderer.drawString(s, 0, 0, colour);
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();
	}

	public static int getMaxWidth(List<String> toDisplay) {
		int width = 0;
		for (String s : toDisplay) {
			int w = RenderHelper.fontRenderer.getStringWidth(s);
			if (w > width) {
				width = w;
			}
		}
		return width;
	}

	public static int getMaxHeight(List<String> toDisplay, int spacing) {
		int height = toDisplay.size() * getStringHeight();
		// FIXME should empty strings just be the spacing height???
		int spacings = toDisplay.size() - 1; /// -1 because the first text has no gap.
		if (spacings > 0) {
			height += spacings * spacing;
		}
		return height;
	}

	/** in pixels */
	public static int getStringWidth(String s) {
		return RenderHelper.fontRenderer.getStringWidth(s);
	}

	/** in pixels */
	public static int getStringHeight() {
		return RenderHelper.fontRenderer.FONT_HEIGHT;
	}

	public static int getStartX(int maxWidth) {

		return 0;
	}

	public static void renderBox(double width, double height) {
		GlStateManager.depthMask(true);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);

		double minX = -barOffset + 0.0625, minY = -barOffset + 0.0625 * 1, maxX = width + barOffset + 0.0625 * 1, maxY = height + barOffset + 0.0625 * 1;
		double barWidth = (maxX - minX);
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

	public static void renderProgressBar(double width, double height, double d, double e) {
		GlStateManager.depthMask(true);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
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

	public static void renderProgressBarWithSprite(TextureAtlasSprite sprite, double width, double height, double progress, double maxProgress) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
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

	public static void rotateDisplayRendering(EnumFacing face, EnumFacing rotation, double width, double height) {
		double[] translate = matrix[face.ordinal()];
		GL11.glRotated(180, 0, 0, 1);
		switch (face) {
		case DOWN:
			GL11.glRotated(90, 1, 0, 0);
			int ordinal = rotation.ordinal();
			ordinal = ordinal == 4 ? 5 : ordinal == 5 ? 4 : ordinal;
			GL11.glRotated(rotate[ordinal], 0, 0, 1);
			translate = getDownMatrix(ordinal, Math.max(1D, width), Math.max(1D, height));
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
		GL11.glTranslated(translate[0], translate[1], translate[2]);
	}

	public static double[] getDownMatrix(int i, double width, double height) {
		double[][] newMatrix = new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, width, 0 }, { height, 0, 0 }, { 0, 0, 0 }, { height, width, 0 } };
		return newMatrix[i];
	}

	public static double[] getUpMatrix(int i, double width, double height) {
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
			} else if (directInfo instanceof LogicInfo) {
				String category = ((LogicInfo) directInfo).getRegistryType().name();
				FontHelper.text(category.substring(0, 1) + category.substring(1).toLowerCase(), identifierLeft + 4, yPos, colour);
			}
		} else if (info instanceof MonitoredBlockCoords) {
			MonitoredBlockCoords directInfo = (MonitoredBlockCoords) info;
			FontHelper.text(directInfo.getCoords().toString(), identifierLeft, yPos, colour);
		}
	}

	public static final double ITEM_SPACING = 22.7;
	public static final double FLUID_DIMENSION = (14 * 0.0625);

	public static void renderInventory(List<MonitoredItemStack> cachedList, int start, int stop, int xSlots, int ySlots) {
		pushMatrix();
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
			MonitoredItemStack stack = cachedList.get(i);
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
			MonitoredItemStack stack = cachedList.get(i);
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
