package sonar.logistics.info.types;

import java.util.ArrayList;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.sync.SyncUUID;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.DisplayButton;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.ScreenInteractionEvent;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.readers.ILogicMonitor;
import sonar.logistics.client.gui.GuiInventoryReader;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = LogicInfoList.id, modid = Logistics.MODID)
public class LogicInfoList extends BaseInfo<LogicInfoList> implements INameableInfo<LogicInfoList>, IAdvancedClickableInfo {

	public static final String id = "logiclist";
	public SyncUUID monitorUUID = new SyncUUID(0);
	public SyncTagType.STRING infoID = new SyncTagType.STRING(1);
	public final SyncTagType.INT networkID = (INT) new SyncTagType.INT(2).setDefault(-1);
	public int pageCount = 0;
	public int xSlots, ySlots, perPage;

	{
		syncList.addParts(monitorUUID, infoID, networkID);
	}

	public LogicInfoList() {
	}

	public LogicInfoList(UUID monitorUUID, String infoID, int networkID) {
		this.monitorUUID.setObject(monitorUUID);
		this.infoID.setObject(infoID);
		this.networkID.setObject(networkID);
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isIdenticalInfo(LogicInfoList info) {
		return monitorUUID.getUUID().equals(info.monitorUUID.getUUID());
	}

	@Override
	public boolean isMatchingInfo(LogicInfoList info) {
		return infoID.getObject() == info.infoID.getObject();
	}

	@Override
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof LogicInfoList;
	}

	@Override
	public LogicMonitorHandler<LogicInfoList> getHandler() {
		return null;
	}

	@Override
	public boolean isValid() {
		return monitorUUID.getUUID() != null;
	}

	@Override
	public LogicInfoList copy() {
		return new LogicInfoList(monitorUUID.getUUID(), infoID.getObject(), networkID.getObject());
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {

		if (displayMenu) {
			renderButtons(container, displayInfo, width, height, scale, infoPos);
			return;
		}

		MonitoredList<?> list = Logistics.getClientManager().getMonitoredList(networkID.getObject(), displayInfo.getInfoUUID());
		ILogicMonitor monitor = Logistics.getClientManager().monitors.get(monitorUUID.getUUID());

		if (monitor == null || list == null)
			return;
		if (infoID.getObject().equals(MonitoredItemStack.id)) {
			if (list == null || list.isEmpty()) {
				// new InfoError("NO ITEMS").renderInfo(displayType, width, height, scale, infoPos);
				return;
			}
			xSlots = (int) Math.ceil(width * 2);
			ySlots = (int) (Math.round(height * 2));
			perPage = xSlots * ySlots;
			double spacing = 22.7;

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPushMatrix();
			GL11.glTranslated(-1 + (0.0625 * 1.3), -1 + 0.0625 * 5, 0.00);
			GL11.glRotated(180, 0, 1, 0);
			GL11.glScaled(-1, 1, 1);
			GlStateManager.enableDepth();
			MonitoredList<MonitoredItemStack> stacks = (MonitoredList<MonitoredItemStack>) list.copyInfo();
			if (stacks.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
			for (int i = perPage * pageCount; i < Math.min(perPage + perPage * pageCount, stacks.size()); i++) {
				MonitoredItemStack stack = stacks.get(i);
				if (stack.isValid()) {
					int current = i - perPage * pageCount;
					StoredItemStack item = stack.itemStack.getObject();
					int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
					int yLevel = (int) (Math.floor((current / xSlots)));
					GL11.glPushMatrix();
					GL11.glScaled(0.022, 0.022, 0.01);
					GL11.glTranslated(xLevel * spacing, yLevel * spacing, 0);
					GlStateManager.disableLighting();
					GlStateManager.enableCull();
					GlStateManager.enablePolygonOffset();
					GlStateManager.doPolygonOffset(-1, -1);
					RenderHelper.renderItemIntoGUI(item.item, 0, 0);
					GlStateManager.disablePolygonOffset();
					GlStateManager.translate(0, 0, 1);
					//GlStateManager.depthMask(false);
					RenderHelper.renderStoredItemStackOverlay(item.item, 0, 0, 0, "" + item.stored, false);
					//GlStateManager.depthMask(true);
					GL11.glPopMatrix();
				}
			}
			GlStateManager.enableDepth();
			GL11.glPopMatrix();
		}
		if (infoID.getObject().equals(MonitoredFluidStack.id)) {
			MonitoredList<MonitoredFluidStack> fluids = (MonitoredList<MonitoredFluidStack>) list.copyInfo();
			double dimension = (14 * 0.0625);
			xSlots = (int) Math.round(width);
			ySlots = (int) (Math.round(height));
			perPage = xSlots * ySlots;

			if (fluids.size() < perPage * pageCount) {
				pageCount = 0;
			}
			for (int i = perPage * pageCount; i < Math.min(perPage + perPage * pageCount, fluids.size()); i++) {
				MonitoredFluidStack fluid = fluids.get(i);
				GL11.glPushMatrix();
				int current = i - perPage * pageCount;
				int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
				int yLevel = (int) (Math.floor((current / xSlots)));
				GL11.glTranslated(xLevel, yLevel, 0);

				// fluid.renderInfo(container, displayInfo, dimension, dimension, 0.012, infoPos);

				FluidStack stack = fluid.fluidStack.getObject().fluid;
				if (stack != null) {
					GL11.glPushMatrix();
					GL11.glPushMatrix();
					GlStateManager.disableLighting();
					GL11.glTranslated(-1, -0.0625 * 12, +0.004);
					TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(stack.getFluid().getStill().toString());
					Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					InfoRenderer.renderProgressBarWithSprite(sprite, dimension, dimension, 0.012, fluid.fluidStack.getObject().stored, fluid.fluidStack.getObject().capacity);
					GlStateManager.enableLighting();
					GL11.glTranslated(0, 0, -0.001);
					GL11.glPopMatrix();
					InfoRenderer.renderNormalInfo(container.display.getDisplayType(), dimension, dimension + 0.0625, 0.012, fluid.getClientIdentifier(), fluid.getClientObject());
					GL11.glPopMatrix();
					GL11.glPopMatrix();
				}
			}
		}
	}

	@Override
	public NBTTagCompound onClientClick(ScreenInteractionEvent event, IDisplayInfo renderInfo, EntityPlayer player, ItemStack stack, InfoContainer container) {
		NBTTagCompound clickTag = new NBTTagCompound();
		if (event.type == BlockInteractionType.SHIFT_RIGHT) {
			MonitoredList<?> list = Logistics.getClientManager().getMonitoredList(networkID.getObject(), renderInfo.getInfoUUID());
			/* displayMenu=!displayMenu; this.resetButtons(); */
			if (list.size() > perPage * (pageCount + 1)) {
				this.pageCount++;
			}else{
				this.pageCount=0;
			}
			player.addChatComponentMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "PAGE " + (pageCount+1) +" of " + (list.size()/perPage )));
			return clickTag;
		}
		if (displayMenu) {
			return clickTag;
		}
		if (infoID.getObject().equals(MonitoredItemStack.id) && event.hit != null) {
			int slot = (perPage * pageCount) + CableHelper.getSlot(container.getDisplay(), renderInfo.getRenderProperties(), event.hit.hitVec, 2, 2);
			MonitoredList<?> list = Logistics.getClientManager().getMonitoredList(networkID.getObject(), renderInfo.getInfoUUID());
			if (list != null && slot >= 0 && slot < list.size()) {
				MonitoredItemStack itemStack = (MonitoredItemStack) list.get(slot);
				if (itemStack != null) {
					itemStack.writeData(clickTag, SyncType.SAVE);
				}
				return clickTag;
			}
		} else if (infoID.getObject().equals(MonitoredFluidStack.id) && event.hit != null) {
			int slot = (perPage * pageCount) + CableHelper.getSlot(container.getDisplay(), renderInfo.getRenderProperties(), event.hit.hitVec, 1, 1);
			MonitoredList<?> list = Logistics.getClientManager().getMonitoredList(networkID.getObject(), renderInfo.getInfoUUID());
			if (list != null && slot >= 0 && slot < list.size()) {
				MonitoredFluidStack fluidStack = (MonitoredFluidStack) list.get(slot);
				if (fluidStack != null) {
					fluidStack.writeData(clickTag, SyncType.SAVE);
				}
				return clickTag;
			}
		}
		return clickTag;
	}

	@Override
	public void onClickEvent(InfoContainer container, IDisplayInfo displayInfo, ScreenInteractionEvent event, NBTTagCompound clickTag) {
		if (infoID.getObject().equals(MonitoredItemStack.id)) {
			MonitoredItemStack clicked = NBTHelper.instanceNBTSyncable(MonitoredItemStack.class, clickTag);
			InfoHelper.screenItemStackClicked(clicked.itemStack.getObject(), networkID.getObject(), event.type, event.doubleClick, displayInfo.getRenderProperties(), event.player, event.hand, event.player.getHeldItem(event.hand), event.hit);
		} else if (infoID.getObject().equals(MonitoredFluidStack.id)) {
			MonitoredFluidStack clicked = NBTHelper.instanceNBTSyncable(MonitoredFluidStack.class, clickTag);
			InfoHelper.screenFluidStackClicked(clicked.fluidStack.getObject(), networkID.getObject(), event.type, event.doubleClick, displayInfo.getRenderProperties(), event.player, event.hand, event.player.getHeldItem(event.hand), event.hit);
		}
	}

	@SideOnly(Side.CLIENT)
	public void getButtons(ArrayList<DisplayButton> buttons) {
		super.getButtons(buttons);
		buttons.add(new DisplayButton("nxPg", 16, 16, "Next Page"));
		buttons.add(new DisplayButton("pvPg", 16, 16, "Previous Page"));
	}

	@Override
	public String getClientIdentifier() {
		return "List: " + infoID.getObject().toLowerCase();
	}

	@Override
	public String getClientObject() {
		/* Pair<ILogicMonitor, MonitoredList<?>> monitor = LogicMonitorManager.getMonitorFromServer(monitorUUID.getUUID().hashCode()); return "Size: " + (monitor != null && monitor.b != null ? monitor.b.size() : 0); */
		return "LIST";
	}

	@Override
	public String getClientType() {
		return "list";
	}

}