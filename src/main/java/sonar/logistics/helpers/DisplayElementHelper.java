package sonar.logistics.helpers;

import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.SonarCore;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.text.IStyledString;
import sonar.logistics.api.displays.elements.text.StyledString;
import sonar.logistics.api.displays.elements.text.StyledStringLine;

public class DisplayElementHelper {

	public static int getRegisteredID(IDisplayElement info) {
		return PL2ASMLoader.elementIDs.get(info.getRegisteredName());
	}

	public static Class<? extends IDisplayElement> getElementClass(int id) {
		return PL2ASMLoader.elementIClasses.get(id);
	}

	public static NBTTagCompound saveElement(NBTTagCompound tag, IDisplayElement info, SyncType type) {
		tag.setInteger("EiD", getRegisteredID(info));
		return info.writeData(tag, type);
	}

	public static IDisplayElement loadElement(NBTTagCompound tag, IElementStorageHolder holder) {
		int elementID = tag.getInteger("EiD");
		return instanceDisplayElement(getElementClass(elementID), holder, tag);
	}

	@Nullable
	public static <T extends IDisplayElement> T instanceDisplayElement(Class<T> classType, IElementStorageHolder holder, NBTTagCompound tag) {
		T obj = null;
		try {
			obj = classType.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			SonarCore.logger.error("FAILED TO CREATE NEW INSTANCE OF " + classType.getSimpleName());
		}
		if (obj != null) {
			obj.setHolder(holder);
			obj.readData(tag, SyncType.SAVE);
			return obj;
		}
		return null;
	}

	public static int getRegisteredID(IStyledString info) {
		return PL2ASMLoader.sstringIDs.get(info.getRegisteredName());
	}

	public static Class<? extends IStyledString> getStyledStringClass(int id) {
		return PL2ASMLoader.sstringIClasses.get(id);
	}

	public static NBTTagCompound saveStyledString(NBTTagCompound tag, IStyledString string, SyncType type) {
		if (!(string instanceof StyledString)) {
			tag.setInteger("SSiD", getRegisteredID(string));
		}
		return string.writeData(tag, type);
	}

	public static IStyledString loadStyledString(StyledStringLine line, NBTTagCompound tag) {
		int elementID = tag.getInteger("SSiD");
		Class<? extends IStyledString> clazz = elementID == 0 ? StyledString.class : getStyledStringClass(elementID);
		return instanceStyledString(clazz, line, tag);
	}

	@Nullable
	public static <T extends IStyledString> T instanceStyledString(Class<T> classType, StyledStringLine line, NBTTagCompound tag) {
		T obj = null;
		try {
			obj = classType.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			SonarCore.logger.error("FAILED TO CREATE NEW INSTANCE OF " + classType.getSimpleName());
		}
		if (obj != null) {
			obj.setLine(line);
			obj.readData(tag, SyncType.SAVE);
			return obj;
		}
		return null;
	}

	public static int getRegisteredID(IDisplayAction info) {
		return PL2ASMLoader.displayActionIDs.get(info.getRegisteredName());
	}

	public static Class<? extends IDisplayAction> getDisplayActionClass(int id) {
		return PL2ASMLoader.displayActionIClasses.get(id);
	}

	public static NBTTagCompound saveDisplayAction(NBTTagCompound tag, IDisplayAction info, SyncType type) {
		tag.setInteger("AiD", getRegisteredID(info));
		return info.writeData(tag, type);
	}

	public static IDisplayAction loadDisplayAction(NBTTagCompound tag) {
		int elementID = tag.getInteger("AiD");
		return instanceDisplayAction(getDisplayActionClass(elementID), tag);
	}

	@Nullable
	public static <T extends IDisplayAction> T instanceDisplayAction(Class<T> classType, NBTTagCompound tag) {
		T obj = null;
		try {
			obj = classType.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			SonarCore.logger.error("FAILED TO CREATE NEW INSTANCE OF " + classType.getSimpleName());
		}
		if (obj != null) {
			obj.readData(tag, SyncType.SAVE);
			return obj;
		}
		return null;
	}

	public static double[] scaleFromPercentage(double[] percentage, double[] toFit) {
		double[] scale = new double[percentage.length];
		int max = Math.min(percentage.length, toFit.length);
		for (int i = 0; i < max; i++) {
			double p = percentage[i];
			double s = toFit[i];
			scale[i] = (((double) s / 100D) * p);
		}
		return scale;
	}

	public static double[] percentageFromScale(double[] size, double[] maximum) {
		double[] percentage = new double[size.length];
		int max = Math.min(size.length, maximum.length);
		for (int i = 0; i < max; i++) {
			double s = size[i];
			double m = maximum[i];
			percentage[i] = (Math.min(s, m) / Math.max(s, m)) * 100D;
		}
		return percentage;
	}

	public static double[] toNearestPixel(double[] original, double[] max) {
		return toNearestMultiple(original, max, 0.0625D);
	}

	public static double toNearestPixel(double original, double max) {
		return toNearestMultiple(original, max, 0.0625D);
	}

	public static double[] toNearestMultiple(double[] original, double[] max, double multiple) {
		double[] scale = new double[original.length];
		for (int i = 0; i < scale.length; i++) {
			scale[i] = toNearestMultiple(original[i], max[i], multiple);
		}
		return scale;
	}

	public static double toNearestMultiple(double original, double max, double multiple) {
		return Math.min(multiple * (Math.floor(Math.abs(original / multiple))), max);
	}

	public static double[] scaleArray(double[] toScale, double scale) {
		double[] newScale = new double[toScale.length];
		for (int i = 0; i < newScale.length; i++) {
			newScale[i] = toScale[i] * scale;
		}
		return newScale;
	}

	public static final int WIDTH = 0, HEIGHT = 1, SCALE = 2;

	/** @param holder the holder to render */
	public static void renderElementStorageHolder(IElementStorageHolder holder) {
		holder.getElements().forEach(e -> renderElementInHolder(holder, e));
	}

	public static void renderElementInHolder(IElementStorageHolder holder, IDisplayElement e) {
		holder.startElementRender(e);
		pushMatrix();
		align(holder.getAlignmentTranslation(e));
		double scale = e.getActualScaling()[SCALE];
		scale(scale, scale, scale);
		// pushMatrix();
		e.render();
		// popMatrix();
		popMatrix();
		holder.endElementRender(e);

	}

	public static double[] alignArray(double[] actualListScaling, double[] actualElementScaling, WidthAlignment width, HeightAlignment height) {
		double x = alignWidth(actualListScaling, actualElementScaling, width);
		double y = alignHeight(actualListScaling, actualElementScaling, height);
		double z = 0;
		return new double[] { x, y, z };
	}

	public static void align(double[] actualListScaling, double[] actualElementScaling, WidthAlignment width, HeightAlignment height) {
		double[] alignArray = alignArray(actualListScaling, actualElementScaling, width, height);
		align(alignArray);
	}

	public static void align(double[] align) {
		translate(align[WIDTH], align[HEIGHT], 0);
	}

	public static double alignWidth(double[] actualListScaling, double[] actualElementScaling, WidthAlignment align) {
		switch (align) {
		case CENTERED:
			return (actualListScaling[WIDTH] / 2) - (actualElementScaling[WIDTH] / 2);
		case LEFT:
			break;
		case RIGHT:
			return actualListScaling[WIDTH] - actualElementScaling[WIDTH];
		}
		return 0;
	}

	public static double alignHeight(double[] actualListScaling, double[] actualElementScaling, HeightAlignment align) {
		switch (align) {
		case CENTERED:
			return (actualListScaling[HEIGHT] / 2) - (actualElementScaling[HEIGHT] / 2);
		case TOP:
			break;
		case BOTTOM:
			return actualListScaling[HEIGHT] - actualElementScaling[HEIGHT];
		}
		return 0;
	}

	/** scales the unscaled width and height to match the given scaling returned in the form, actual width, actual height, scale factor */
	public static double[] getScaling(int[] unscaled, double[] scaling, double percentageFill) {
		double actualElementScale = Math.min(scaling[0] / unscaled[0], scaling[1] / unscaled[1]);
		double actualElementWidth = (unscaled[0] * actualElementScale) * percentageFill;
		double actualElementHeight = (unscaled[1] * actualElementScale) * percentageFill;
		return new double[] { actualElementWidth, actualElementHeight, actualElementScale };
	}

	public static void drawGrid(double left, double top, double right, double bottom, double xSizing, double ySizing, int color) {
		if (left < right) {
			double i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			double j = top;
			top = bottom;
			bottom = j;
		}
		int yElements = (int) (Math.max(top, bottom) / ySizing);
		int xElements = (int) (Math.max(left, right) / xSizing);
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(f, f1, f2, f3);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		for (int i = 0; i < yElements + 1; i++) {

			bufferbuilder.pos(left, i * ySizing - 0.0625 / 4, 0.0D).endVertex();
			bufferbuilder.pos(right, i * ySizing - 0.0625 / 4, 0.0D).endVertex();
			bufferbuilder.pos(right, (i * ySizing) + 0.0625 / 4, 0.0D).endVertex();
			bufferbuilder.pos(left, (i * ySizing) + 0.0625 / 4, 0.0D).endVertex();
		}
		for (int i = 0; i < xElements + 1; i++) {
			bufferbuilder.pos((i * xSizing) + 0.0625 / 4, bottom, 0.0D).endVertex();
			bufferbuilder.pos(i * xSizing - 0.0625 / 4, bottom, 0.0D).endVertex();
			bufferbuilder.pos(i * xSizing - 0.0625 / 4, top, 0.0D).endVertex();
			bufferbuilder.pos((i * xSizing) + 0.0625 / 4, top, 0.0D).endVertex();
		}
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}

	public static void drawRect(double left, double top, double right, double bottom, int color) {
		if (left < right) {
			double i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			double j = top;
			top = bottom;
			bottom = j;
		}
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		GlStateManager.disableLighting();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		RenderHelper.saveBlendState();
		// GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
		// GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		GlStateManager.color(f, f1, f2, f3);

		bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(left, bottom, 0.0D).endVertex();
		bufferbuilder.pos(right, bottom, 0.0D).endVertex();
		bufferbuilder.pos(right, top, 0.0D).endVertex();
		bufferbuilder.pos(left, top, 0.0D).endVertex();
		tessellator.draw();

		GlStateManager.color(255, 255, 255, 255);
		GlStateManager.resetColor();
		RenderHelper.restoreBlendState();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		GlStateManager.enableLighting();
	}
}
