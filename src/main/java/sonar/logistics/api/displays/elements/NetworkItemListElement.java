package sonar.logistics.api.displays.elements;

import static net.minecraft.client.renderer.GlStateManager.*;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.ElementFillType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredItemStack;

@DisplayElementType(id = NetworkItemListElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkItemListElement extends AbstractInfoElement<LogicInfoList> {

	public int pageCount = 0;
	public int xSlots, ySlots, perPage = 0;
	public List<MonitoredItemStack> cachedList = null;

	public NetworkItemListElement() {}

	public NetworkItemListElement(InfoUUID uuid) {
		super(uuid);
	}

	public void render(LogicInfoList list) {
		cachedList = getCachedList(list, uuid);
		double percent = 0.75;
		double sharedSize = 7 * 0.0625;		
		double width = sharedSize;
		double height = sharedSize;
		xSlots = (int)Math.floor(getActualScaling()[WIDTH]/width);
		ySlots = (int)Math.floor(getActualScaling()[HEIGHT]/height);
		double X_SPACING = (getActualScaling()[WIDTH] - (xSlots * width)) / xSlots;
		double Y_SPACING = (getActualScaling()[HEIGHT] - (ySlots * height)) / ySlots;		
		double centreX = (width / 2) - (width * percent / 2);
		double centreY = (height / 2) - (height * percent / 2);
		

		pushMatrix();
		color(1.0F, 1.0F, 1.0F, 1.0F);
		translate(0, 0, -0.01);
		rotate(180, 0, 1, 0); // flips the item
		scale(-1, 1, 1);

		enableRescaleNormal();
		disableLighting();
		

		perPage = xSlots * ySlots;		
		int start = perPage * pageCount, stop = Math.min(perPage + perPage * pageCount, cachedList.size());
		
		for (int i = start; i < stop; i++) {
			MonitoredItemStack stack = cachedList.get(i);
			pushMatrix();
			int current = i - start;
			int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
			int yLevel = (int) (Math.floor((current / xSlots)));
			translate((xLevel * width) + centreX + (X_SPACING*(xLevel+0.5D)), (yLevel * height) + centreY + (Y_SPACING*(yLevel+0.5D)), 0);
			scale((width / 16) * percent, (height / 16) * percent, 0.001);
			disableLighting();//stored itemstack overlay enables it agaain???
			RenderHelper.renderItemIntoGUI(stack.getItemStack(), 0, 0);
			translate(0, 0, 2);
			depthMask(false);
			RenderHelper.renderStoredItemStackOverlay(stack.getItemStack(), 0, 0, 0, "" + stack.getStored(), false);
			depthMask(true);			
			popMatrix();
		}
		enableLighting();
		disableRescaleNormal();
		
		/* double scale = (0.0625*pixelWidth/16); scale(scale, scale, 0.01); RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false); blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA); enableRescaleNormal(); enableAlpha(); alphaFunc(516, 0.1F); enableBlend(); pushMatrix(); for (int i = start; i < stop; i++) { MonitoredItemStack stack = cachedList.get(i); int current = i - start; int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots)); int yLevel = (int) (Math.floor((current / xSlots))); pushMatrix(); GL11.glTranslated(xLevel * ITEM_SPACING, yLevel * ITEM_SPACING, 0); disableLighting(); InfoRenderer.renderItemModelIntoGUI(stack.getItemStack(), 0, 0); popMatrix(); } popMatrix(); disableAlpha(); disableRescaleNormal(); disableLighting(); disableBlend(); */
		RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

		/* translate(0, 0, -1); depthMask(false); pushMatrix(); final float scaleFactor = 0.5F; final float inverseScaleFactor = 1.0f / scaleFactor; scale(scaleFactor, scaleFactor, scaleFactor); for (int i = start; i < stop; i++) { MonitoredItemStack stack = cachedList.get(i); int current = i - start; int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots)); int yLevel = (int) (Math.floor((current / xSlots))); pushMatrix(); translate((xLevel * ITEM_SPACING) * inverseScaleFactor, (yLevel * ITEM_SPACING) * inverseScaleFactor, 0); String s = "" + stack.getStored(); final int X = (int) (((float) 0 + 15.0f - RenderHelper.fontRenderer.getStringWidth(s) * scaleFactor) * inverseScaleFactor); final int Y = (int) (((float) 0 + 15.0f - 7.0f * scaleFactor) * inverseScaleFactor); RenderHelper.fontRenderer.drawStringWithShadow(s, X, Y, 16777215); popMatrix(); } popMatrix(); depthMask(true); */
		popMatrix();

	}

	public List<MonitoredItemStack> getCachedList(LogicInfoList info, InfoUUID id) {
		if (cachedList == null || info.listChanged) {
			info.listChanged = false;
			AbstractChangeableList<?> list = PL2.getInfoManager(true).getMonitoredList(id);
			cachedList = list != null ? (ArrayList<MonitoredItemStack>) list.createSaveableList() : Lists.newArrayList();
			if (cachedList.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
		}
		return cachedList;
	}

	@Override
	public boolean isType(IInfo info) {
		return info instanceof LogicInfoList;
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_CONTAINER;
	}

	public static final String REGISTRY_NAME = "n_item_l";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
