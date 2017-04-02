package sonar.logistics.info.types;

import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.sync.SyncUUID;
import sonar.core.utils.CustomColour;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.ScreenInteractionEvent;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredEnergyStack;
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

	public MonitoredList<?> getCachedList(InfoUUID id) {
		return Logistics.getClientManager().getMonitoredList(networkID.getObject(), id);
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		/*
		if (displayMenu) {
			renderButtons(container, displayInfo, width, height, scale, infoPos);
			return;
		}
		*/
		MonitoredList<?> list = getCachedList(displayInfo.getInfoUUID());
		if (list == null)
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
			GlStateManager.pushAttrib();
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
					StoredItemStack item = stack.getStoredStack();
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
					GlStateManager.depthMask(false);
					RenderHelper.renderStoredItemStackOverlay(item.item, 0, 0, 0, "" + item.stored, false);
					GlStateManager.depthMask(true);
					GL11.glPopMatrix();
				}
			}
			GlStateManager.disableDepth();
			GlStateManager.popAttrib();
			GL11.glPopMatrix();
		} else if (infoID.getObject().equals(MonitoredFluidStack.id)) {
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

				FluidStack stack = fluid.getStoredStack().fluid;
				if (stack != null) {
					GL11.glPushMatrix();
					GL11.glPushMatrix();
					GlStateManager.disableLighting();
					GL11.glTranslated(-1, -0.0625 * 12, +0.004);
					TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(stack.getFluid().getStill().toString());
					Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					InfoRenderer.renderProgressBarWithSprite(sprite, dimension, dimension, 0.012, fluid.getStored(), fluid.getStoredStack().capacity);
					GlStateManager.enableLighting();
					GL11.glTranslated(0, 0, -0.001);
					GL11.glPopMatrix();
					InfoRenderer.renderNormalInfo(container.display.getDisplayType(), dimension, dimension + 0.0625, 0.012, fluid.getClientIdentifier(), fluid.getClientObject());
					GL11.glPopMatrix();
					GL11.glPopMatrix();
				}
			}
		} else if (infoID.getObject().equals(MonitoredEnergyStack.id)) {
			MonitoredList<MonitoredEnergyStack> energy = (MonitoredList<MonitoredEnergyStack>) list.copyInfo();
			xSlots = (int) 1;
			ySlots = (int) ((Math.round(height)) / (0.0625 * 7));
			perPage = xSlots * ySlots;
			double spacing = 0.0625 * 7;
			if (energy.size() < perPage * pageCount) {
				pageCount = 0;
			}
			GL11.glTranslated(-1, -1 + 0.0625 * 4, 0.00);
			int end = Math.min(perPage + perPage * pageCount, energy.size());
			for (int i = perPage * pageCount; i < end; i++) {
				MonitoredEnergyStack info = energy.get(i);
				int current = i - perPage * pageCount;
				int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
				int yLevel = (int) (Math.floor((current / xSlots)));
				GL11.glPushMatrix();
				GL11.glTranslated(xLevel * spacing, yLevel * spacing, 0);
				double l = ((double) info.energyStack.obj.stored * (double) (width) / info.energyStack.obj.capacity);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderHelper.saveBlendState();

				GlStateManager.disableLighting();
				boolean isHighlighted = false;
				if (!RenderBlockSelection.positions.isEmpty()) {
					if (RenderBlockSelection.isPositionRenderered(info.coords.getMonitoredInfo().syncCoords.getCoords())) {
						isHighlighted=true;						
					}
				}
				Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(2));
				InfoRenderer.renderProgressBar(width, 6 * 0.0625, scale, l, width);
				RenderHelper.restoreBlendState();
				GL11.glTranslated(0, 0, -0.00625);

				// GL11.glTranslated((width/2)-1, +1 + 0.0625 * 4, 0.00);
				GL11.glTranslated(1, 1 - 0.0625 * 3.5, 0.00);
				InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, 0.0625 * 6, scale / 3, isHighlighted? new CustomColour(20, 100, 180).getRGB() : -1, Lists.newArrayList(info.coords.getMonitoredInfo().getClientIdentifier() + " - " + info.coords.getMonitoredInfo().getClientObject(), info.getClientIdentifier() + " - " + info.getClientObject()));
				GL11.glPopMatrix();

			}
		}
	}

	@Override
	public NBTTagCompound onClientClick(ScreenInteractionEvent event, IDisplayInfo renderInfo, EntityPlayer player, ItemStack stack, InfoContainer container) {
		NBTTagCompound clickTag = new NBTTagCompound();
		if (event.type == BlockInteractionType.SHIFT_RIGHT) {
			MonitoredList<?> list = getCachedList(renderInfo.getInfoUUID());
			/* displayMenu=!displayMenu; this.resetButtons(); */
			if (list.size() > perPage * (pageCount + 1)) {
				this.pageCount++;
			} else {
				this.pageCount = 0;
			}
			player.addChatComponentMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "PAGE " + (pageCount + 1) + " of " + Math.min(pageCount + 1, Math.round((double) list.size() / Math.max(perPage, 1)))));
			return clickTag;
		}
		/*
		if (displayMenu) {
			return clickTag;
		}
		*/
		if (infoID.getObject().equals(MonitoredItemStack.id) && event.hit != null) {
			int slot = (perPage * pageCount) + CableHelper.getSlot(container.getDisplay(), renderInfo.getRenderProperties(), event.hit.hitVec, 2, 2);
			MonitoredList<?> list = getCachedList(renderInfo.getInfoUUID());
			if (list != null && slot >= 0 && slot < list.size()) {
				MonitoredItemStack itemStack = (MonitoredItemStack) list.get(slot);
				if (itemStack != null) {
					itemStack.writeData(clickTag, SyncType.SAVE);
				}
				return clickTag;
			}
		} else if (infoID.getObject().equals(MonitoredFluidStack.id) && event.hit != null) {
			int slot = (perPage * pageCount) + CableHelper.getSlot(container.getDisplay(), renderInfo.getRenderProperties(), event.hit.hitVec, 1, 1);
			MonitoredList<?> list = getCachedList(renderInfo.getInfoUUID());
			if (list != null && slot >= 0 && slot < list.size()) {
				MonitoredFluidStack fluidStack = (MonitoredFluidStack) list.get(slot);
				if (fluidStack != null) {
					fluidStack.writeData(clickTag, SyncType.SAVE);
				}
				return clickTag;
			}
		} else if (infoID.getObject().equals(MonitoredEnergyStack.id) && event.hit != null) {
			int slot = (int) ((perPage * pageCount) + CableHelper.getListSlot(container.getDisplay(), renderInfo.getRenderProperties(), event.hit.hitVec, 0.0625 * 6, 0.0625 * 1, perPage));
			MonitoredList<?> list = getCachedList(renderInfo.getInfoUUID());
			if (list != null && slot >= 0 && slot < list.size()) {
				MonitoredEnergyStack energyStack = (MonitoredEnergyStack) list.get(slot);
				if (energyStack != null) {
					if (event.type == BlockInteractionType.RIGHT) {
						RenderBlockSelection.addPosition(energyStack.coords.getMonitoredInfo().syncCoords.getCoords(), false);
						player.addChatComponentMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "'"  +energyStack.coords.getMonitoredInfo().getClientIdentifier() + "'" + " has been highlighted"));

					}
				}
				// MonitoredEnergyStack energyStack = (MonitoredEnergyStack) list.get(slot);
				// if (energyStack != null) {
				// energyStack.writeData(clickTag, SyncType.SAVE);
				// }
				return clickTag;
			}
		}
		return clickTag;
	}

	@Override
	public void onClickEvent(InfoContainer container, IDisplayInfo displayInfo, ScreenInteractionEvent event, NBTTagCompound clickTag) {
		if (infoID.getObject().equals(MonitoredItemStack.id)) {
			MonitoredItemStack clicked = NBTHelper.instanceNBTSyncable(MonitoredItemStack.class, clickTag);
			InfoHelper.screenItemStackClicked(clicked.getStoredStack(), networkID.getObject(), event.type, event.doubleClick, displayInfo.getRenderProperties(), event.player, event.hand, event.player.getHeldItem(event.hand), event.hit);
		} else if (infoID.getObject().equals(MonitoredFluidStack.id)) {
			MonitoredFluidStack clicked = NBTHelper.instanceNBTSyncable(MonitoredFluidStack.class, clickTag);
			InfoHelper.screenFluidStackClicked(clicked.getStoredStack(), networkID.getObject(), event.type, event.doubleClick, displayInfo.getRenderProperties(), event.player, event.hand, event.player.getHeldItem(event.hand), event.hit);
		}
	}
	/*
	@SideOnly(Side.CLIENT)
	public void getButtons(ArrayList<DisplayButton> buttons) {
		super.getButtons(buttons);
		buttons.add(new DisplayButton("nxPg", 16, 16, "Next Page"));
		buttons.add(new DisplayButton("pvPg", 16, 16, "Previous Page"));
	}
	*/
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