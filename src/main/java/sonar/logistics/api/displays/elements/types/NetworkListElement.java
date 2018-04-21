package sonar.logistics.api.displays.elements.types;

import static net.minecraft.client.renderer.GlStateManager.color;
import static net.minecraft.client.renderer.GlStateManager.depthMask;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;
import static net.minecraft.client.renderer.GlStateManager.disableRescaleNormal;
import static net.minecraft.client.renderer.GlStateManager.enableLighting;
import static net.minecraft.client.renderer.GlStateManager.enableRescaleNormal;
import static net.minecraft.client.renderer.GlStateManager.popMatrix;
import static net.minecraft.client.renderer.GlStateManager.pushMatrix;
import static net.minecraft.client.renderer.GlStateManager.scale;
import static net.minecraft.client.renderer.GlStateManager.translate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.buttons.ButtonElement;
import sonar.logistics.api.displays.elements.AbstractInfoElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.displays.elements.ILookableElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIClickPacketHelper;
import sonar.logistics.client.gui.display.GuiEditNetworkItemlist;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.info.types.LogicInfoList;

public abstract class NetworkListElement<L> extends AbstractInfoElement<LogicInfoList> implements IClickableElement, ILookableElement {

	public int pageCount = 0;
	public int ySlots, perPage = 0;
	public List<L> cachedList = null;
	public double element_size = 7 * 0.0625;
	public int text_colour = 16777215;
	public double grid_fill_percentage = 0.75;

	public NetworkListElement() {
		super();
	}

	public NetworkListElement(InfoUUID uuid) {
		super(uuid);
	}

	public abstract double getRenderHeight();

	public abstract void renderGridElement(L stack, int index);

	public abstract void onGridElementClicked(DisplayScreenClick click, LogicInfoList list, @Nullable L stack);

	double height, Y_SPACING, centreY;
	int start, stop;

	public void render(LogicInfoList list) {
		info = getGSI().getCachedInfo(uuid);
		cachedList = getCachedList(list, uuid);
		if(cachedList.isEmpty()){
			return;
		}
		height = getRenderHeight();
		centreY = (height / 2) - ((height * grid_fill_percentage) / 2);

		ySlots = (int) Math.floor(getActualScaling()[HEIGHT] / height);
		Y_SPACING = (getActualScaling()[HEIGHT] - (ySlots * height)) / ySlots;

		perPage = ySlots;
		boolean needsPages = perPage < cachedList.size();

		if (needsPages && perPage != 0) {
			double adjusted_height = getActualScaling()[HEIGHT] - (getActualScaling()[HEIGHT] / 8);
			ySlots = (int) Math.floor(adjusted_height / height);
			Y_SPACING = (adjusted_height - (ySlots * height)) / ySlots;
		}
		perPage = ySlots;

		int totalPages = (int) (Math.ceil((double) cachedList.size() / (double) perPage));
		if (pageCount >= totalPages) {
			pageCount = totalPages - 1;
		}

		start = Math.max(perPage * pageCount, 0);
		stop = Math.max(Math.min(perPage + perPage * pageCount, cachedList.size()), 0);
		
		preListRender();
		for (int i = start; i < stop; i++) {
			pushMatrix();
			int index = i - start;
			translate(0, (index * height) + centreY + (Y_SPACING * (index + 0.5D)), 0);
			renderGridElement(cachedList.get(i), index);
			popMatrix();
		}
		postListRender();

		if (needsPages && perPage != 0) {
			DisplayElementHelper.renderPageButons(getActualScaling(), this.pageCount + 1, totalPages);
		}

	}

	public void preListRender() {
		pushMatrix();
		color(1.0F, 1.0F, 1.0F, 1.0F);
		scale(1, 1, -1);
		enableRescaleNormal();
		disableLighting();
	}

	public void postListRender() {
		enableLighting();
		disableRescaleNormal();
		RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		popMatrix();
	}

	public List<L> getCachedList(LogicInfoList info, InfoUUID id) {
		if (cachedList == null || info.listChanged) {
			info.listChanged = false;
			AbstractChangeableList<?> list = PL2.proxy.getInfoManager(true).getMonitoredList(id);
			cachedList = list != null ? (ArrayList<L>) list.createSaveableList(info.listSorter) : new ArrayList<>();
			if (cachedList.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
		}
		AbstractChangeableList<?> list = PL2.proxy.getInfoManager(true).getMonitoredList(id);
		cachedList = list != null ? (ArrayList<L>) list.createSaveableList(info.listSorter) : new ArrayList<>();
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

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		int ySlot = 0;
		for (int y = 0; y < ySlots; y++) {
			double yStart = (y * height) + centreY + (Y_SPACING * (y + 0.5D));
			double yStop = yStart + height;
			if (subClickY >= yStart && subClickY < yStop) {
				ySlot = y;
				break;
			}
		}
		int slot = ySlot + start;
		if (info != null && info instanceof LogicInfoList) {
			LogicInfoList list = (LogicInfoList) info;
			double[] align = this.getHolder().getAlignmentTranslation(this);
			DisplayScreenClick subClick = new DisplayScreenClick().setClickPosition(new double[] { click.clickX - 0.5, click.clickY });
			subClick.setContainerIdentity(click.identity);
			subClick.setDoubleClick(click.doubleClick);
			subClick.gsi = click.gsi;
			subClick.type = click.type;
			subClick.clickPos = click.clickPos;
			L stack = slot < cachedList.size() ? cachedList.get(slot) : null;
			onGridElementClicked(subClick, list, stack);
		}

		boolean needsPages = perPage < cachedList.size();
		if (needsPages) {
			int totalPages = (int) (Math.ceil((double) cachedList.size() / (double) perPage));
			pageCount = DisplayElementHelper.doPageClick(subClickX, subClickY, getActualScaling(), pageCount, totalPages);
		}
		return 0;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		element_size = nbt.getDouble("sizing");
		text_colour = nbt.getInteger("colour");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setDouble("sizing", element_size);
		nbt.setInteger("colour", text_colour);
		return nbt;
	}

}
