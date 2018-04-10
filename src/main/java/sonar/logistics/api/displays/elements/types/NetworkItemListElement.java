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
import sonar.logistics.info.types.MonitoredItemStack;

@DisplayElementType(id = NetworkItemListElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class NetworkItemListElement extends AbstractInfoElement<LogicInfoList> implements IClickableElement {

	public int pageCount = 0;
	public int xSlots, ySlots, perPage = 0;
	public List<MonitoredItemStack> cachedList = null;
	public double sharedSize = 7 * 0.0625;
	public int colour = 16777215;

	public NetworkItemListElement() {}

	public NetworkItemListElement(InfoUUID uuid) {
		super(uuid);
	}

	public double getRenderSize() {
		return Math.min(sharedSize, Math.min(getActualScaling()[0], getActualScaling()[1]));
	}

	public void render(LogicInfoList list) {
		info = getGSI().getCachedInfo(uuid);
		cachedList = getCachedList(list, uuid);
		double percent = 0.75;
		double width = getRenderSize();
		double height = getRenderSize();
		xSlots = (int) Math.floor(getActualScaling()[WIDTH] / width);
		ySlots = (int) Math.floor(getActualScaling()[HEIGHT] / height);
		double X_SPACING = (getActualScaling()[WIDTH] - (xSlots * width)) / xSlots;
		double Y_SPACING = (getActualScaling()[HEIGHT] - (ySlots * height)) / ySlots;
		double centreX = (width / 2) - (width * percent / 2);
		double centreY = (height / 2) - (height * percent / 2);

		pushMatrix();
		color(1.0F, 1.0F, 1.0F, 1.0F);
		scale(1, 1, -1);
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
			translate((xLevel * width) + centreX + (X_SPACING * (xLevel + 0.5D)), (yLevel * height) + centreY + (Y_SPACING * (yLevel + 0.5D)), 0);

			scale((width / 16) * percent, (height / 16) * percent, 0.001);
			disableLighting();// stored itemstack overlay enables it agaain???
			RenderHelper.renderItemIntoGUI(stack.getItemStack(), 0, 0);
			translate(0, 0, 2);
			depthMask(false);
			RenderHelper.renderStoredItemStackOverlay(stack.getItemStack(), 0, 0, 0, colour, "" + stack.getStored(), false);
			depthMask(true);
			popMatrix();
		}
		enableLighting();
		disableRescaleNormal();

		RenderHelper.textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		popMatrix();

	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return GuiSonar.withOrigin(new GuiEditNetworkItemlist(this, obj), origin);
	}

	public List<MonitoredItemStack> getCachedList(LogicInfoList info, InfoUUID id) {
		if (cachedList == null || info.listChanged) {
			info.listChanged = false;
			AbstractChangeableList<?> list = PL2.proxy.getInfoManager(true).getMonitoredList(id);
			cachedList = list != null ? (ArrayList<MonitoredItemStack>) list.createSaveableList() : new ArrayList<>();
			if (cachedList.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
		}
		AbstractChangeableList<?> list = PL2.proxy.getInfoManager(true).getMonitoredList(id);
		cachedList = list != null ? (ArrayList<MonitoredItemStack>) list.createSaveableList() : new ArrayList<>();
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

		double percent = 0.75;
		double width = getRenderSize();
		double height = getRenderSize();
		xSlots = (int) Math.floor(getActualScaling()[WIDTH] / width);
		ySlots = (int) Math.floor(getActualScaling()[HEIGHT] / height);
		double X_SPACING = (getActualScaling()[WIDTH] - (xSlots * width)) / xSlots;
		double Y_SPACING = (getActualScaling()[HEIGHT] - (ySlots * height)) / ySlots;
		double centreX = (width / 2) - (width * percent / 2);
		double centreY = (height / 2) - (height * percent / 2);

		int start = perPage * pageCount, stop = Math.min(perPage + perPage * pageCount, cachedList.size());

		double xCentre = 0, yCentre = 0;
		int xSlot = 0, ySlot = 0;
		for (int x = 0; x < xSlots; x++) {
			double xStart = (x * width) + centreX + (X_SPACING * (x + 0.5D));
			double xStop = xStart + width;
			if (subClickX >= xStart && subClickX < xStop) {
				xSlot = x;
				xCentre = xStart - centreX - width / 2;
				break;
			}
		}
		for (int y = 0; y < ySlots; y++) {
			double yStart = (y * height) + centreY + (Y_SPACING * (y + 0.5D));
			double yStop = yStart + height;
			if (subClickY >= yStart && subClickY < yStop) {
				ySlot = y;
				yCentre = yStart - centreY - height / 2;
				break;
			}
		}
		int slot = ((ySlot * xSlots) + xSlot) + start;
		if (info != null && info instanceof LogicInfoList) {
			LogicInfoList list = (LogicInfoList) info;
			MonitoredItemStack stack = slot < cachedList.size() ? cachedList.get(slot) : null;
			int networkID = (stack == null || stack.getNetworkSource() == -1) ? list.networkID.getObject() : stack.getNetworkSource();
			double[] align = this.getHolder().getAlignmentTranslation(this);
			DisplayScreenClick subClick = new DisplayScreenClick().setClickPosition(new double[] { click.clickX - 0.5, click.clickY });
			subClick.setContainerIdentity(click.identity);
			subClick.setDoubleClick(click.doubleClick);
			subClick.gsi = click.gsi;
			subClick.type = click.type;
			subClick.clickPos = click.clickPos;
			GSIClickPacketHelper.sendGSIClickPacket(GSIClickPacketHelper.createItemClickPacket(stack == null ? null : stack.getStoredStack(), networkID), getHolder().getContainer(), subClick);
		}

		return 0;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		sharedSize = nbt.getDouble("sizing");
		colour = nbt.getInteger("colour");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setDouble("sizing", sharedSize);
		nbt.setInteger("colour", colour);
		return nbt;
	}

	public static final String REGISTRY_NAME = "n_item_l";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
