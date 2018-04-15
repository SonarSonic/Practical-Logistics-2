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

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractInfoElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.displays.elements.IClickableElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIClickPacketHelper;
import sonar.logistics.client.gui.display.GuiEditNetworkItemlist;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.info.types.LogicInfoList;

public abstract class NetworkGridElement<L> extends AbstractInfoElement<LogicInfoList> implements IClickableElement {

	public int pageCount = 0;
	public int xSlots, ySlots, perPage = 0;
	public List<L> cachedList = null;
	public double element_size = 7 * 0.0625;
	public int text_colour = 16777215;
	public double grid_fill_percentage = 0.75;

	public NetworkGridElement() {}

	public NetworkGridElement(InfoUUID uuid) {
		super(uuid);
	}

	public abstract double getRenderWidth();

	public abstract double getRenderHeight();

	public abstract void renderGridElement(L stack, int index);	

	public abstract void onGridElementClicked(DisplayScreenClick click, LogicInfoList list, @Nullable L stack);
	
	double width, height, X_SPACING, Y_SPACING, centreX, centreY;
	int start, stop;

	public void render(LogicInfoList list) {
		info = getGSI().getCachedInfo(uuid);
		cachedList = getCachedList(list, uuid);
		width = getRenderWidth();
		height = getRenderHeight();
		xSlots = (int) Math.floor(getActualScaling()[WIDTH] / width);
		ySlots = (int) Math.floor(getActualScaling()[HEIGHT] / height);
		X_SPACING = (getActualScaling()[WIDTH] - (xSlots * width)) / xSlots;
		Y_SPACING = (getActualScaling()[HEIGHT] - (ySlots * height)) / ySlots;
		centreX = (width / 2) - ((width * grid_fill_percentage) / 2);
		centreY = (height / 2) - ((height * grid_fill_percentage) / 2);

		perPage = xSlots * ySlots;
		start = perPage * pageCount;
		stop = Math.min(perPage + perPage * pageCount, cachedList.size());
		
		preListRender();
		for (int i = start; i < stop; i++) {
			pushMatrix();
			int index = i - start;
			int xLevel = (int) (index - ((Math.floor((index / xSlots))) * xSlots));
			int yLevel = (int) (Math.floor((index / xSlots)));
			translate((xLevel * width) + centreX + (X_SPACING * (xLevel + 0.5D)), (yLevel * height) + centreY + (Y_SPACING * (yLevel + 0.5D)), 0);
			renderGridElement(cachedList.get(i), index);
			popMatrix();
		}
		postListRender();
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
			cachedList = list != null ? (ArrayList<L>) list.createSaveableList() : new ArrayList<>();
			if (cachedList.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
		}
		AbstractChangeableList<?> list = PL2.proxy.getInfoManager(true).getMonitoredList(id);
		cachedList = list != null ? (ArrayList<L>) list.createSaveableList() : new ArrayList<>();
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
		int xSlot = 0, ySlot = 0;
		for (int x = 0; x < xSlots; x++) {
			double xStart = (x * width) + centreX + (X_SPACING * (x + 0.5D));
			double xStop = xStart + width;
			if (subClickX >= xStart && subClickX < xStop) {
				xSlot = x;
				break;
			}
		}
		for (int y = 0; y < ySlots; y++) {
			double yStart = (y * height) + centreY + (Y_SPACING * (y + 0.5D));
			double yStop = yStart + height;
			if (subClickY >= yStart && subClickY < yStop) {
				ySlot = y;
				break;
			}
		}
		int slot = ((ySlot * xSlots) + xSlot) + start;
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
