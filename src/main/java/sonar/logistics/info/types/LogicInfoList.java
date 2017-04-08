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
import sonar.core.utils.SimpleProfiler;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.ScreenInteractionEvent;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredEnergyStack;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = LogicInfoList.id, modid = PL2Constants.MODID)
public class LogicInfoList extends BaseInfo<LogicInfoList> implements INameableInfo<LogicInfoList>, IAdvancedClickableInfo {

	public static final String id = "logiclist";
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncTagType.STRING infoID = new SyncTagType.STRING(1);
	public final SyncTagType.INT networkID = (INT) new SyncTagType.INT(2).setDefault(-1);

	public MonitoredList<?> cachedList = null;
	public boolean listChanged = true;
	public int pageCount = 0;
	public int xSlots, ySlots, perPage;

	// client rendering
	public static final double ITEM_SPACING = 22.7;
	public static final double FLUID_DIMENSION = (14 * 0.0625);
	public Type type = Type.ITEM;

	public enum Type {
		ITEM, FLUID, ENERGY;
	}

	{
		syncList.addParts(identity, infoID, networkID);
	}

	public LogicInfoList() {
		this.setType();
	}

	public LogicInfoList(int identity, String infoID, int networkID) {
		this.identity.setObject(identity);
		this.infoID.setObject(infoID);
		this.networkID.setObject(networkID);
		this.setType();
	}

	public void setType() {
		String infoID = this.infoID.getObject();
		if(infoID==null ||infoID.isEmpty()){
			return;
		}
		if (infoID.equals(MonitoredItemStack.id)) {
			type = Type.ITEM;
		} else if (infoID.equals(MonitoredFluidStack.id)) {
			type = Type.FLUID;
		} else if (infoID.equals(MonitoredEnergyStack.id)) {
			type = Type.ENERGY;
		}
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isIdenticalInfo(LogicInfoList info) {
		return identity == info.identity;
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
		return identity.getObject() != -1;
	}

	@Override
	public LogicInfoList copy() {
		return new LogicInfoList(identity.getObject(), infoID.getObject(), networkID.getObject());
	}

	public MonitoredList<?> getCachedList(InfoUUID id) {
		if (cachedList == null || listChanged) {
			listChanged = false;
			MonitoredList<?> list = PL2.getClientManager().getMonitoredList(networkID.getObject(), id);
			cachedList = list == null ? MonitoredList.newMonitoredList(networkID.getObject()) : (MonitoredList<IMonitorInfo>) list.cloneInfo();
			setType();
			if (cachedList.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
		}
		return cachedList;
	}

	@Override
	public void renderSizeChanged(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderSizeChanged(container, displayInfo, width, height, scale, infoPos);
		switch(type){
		case ENERGY:
			xSlots = (int) 1;
			ySlots = (int) ((Math.round(height)) / (0.0625 * 7));
			perPage = xSlots * ySlots;
			break;
		case FLUID:
			xSlots = (int) Math.round(width);
			ySlots = (int) (Math.round(height));
			perPage = xSlots * ySlots;
			break;
		case ITEM:
			xSlots = (int) Math.ceil(width * 2);
			ySlots = (int) (Math.round(height * 2));
			perPage = xSlots * ySlots;
			break;
		default:
			break;
		}
	}

	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderInfo(container, displayInfo, width, height, scale, infoPos);
		//SimpleProfiler.start("render");
		MonitoredList<?> list = getCachedList(displayInfo.getInfoUUID());
		if (list.isEmpty())
			return;
		switch (type) {
		case ITEM:
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glPushMatrix();
			GL11.glTranslated(-1 + (0.0625 * 1.3), -1 + 0.0625 * 5, 0.00);
			GL11.glRotated(180, 0, 1, 0);
			GL11.glScaled(-1, 1, 1);
			MonitoredList<MonitoredItemStack> stacks = (MonitoredList<MonitoredItemStack>) list;
			for (int i = perPage * pageCount; i < Math.min(perPage + perPage * pageCount, stacks.size()); i++) {
				MonitoredItemStack stack = stacks.get(i);
				if (stack.isValid()) {
					StoredItemStack item = stack.getStoredStack();
					int current = i - perPage * pageCount;
					int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
					int yLevel = (int) (Math.floor((current / xSlots)));
					GL11.glPushMatrix();
					GL11.glScaled(0.022, 0.022, 0.01);
					GL11.glTranslated(xLevel * ITEM_SPACING, yLevel * ITEM_SPACING, 0);
					GlStateManager.disableLighting();
					RenderHelper.renderItemIntoGUI(item.item, 0, 0);
					
					GlStateManager.translate(0, 0, 1);
					GlStateManager.depthMask(false);
					RenderHelper.renderStoredItemStackOverlay(item.item, 0, 0, 0, "" + item.stored, false);
					GlStateManager.depthMask(true);					 
					GL11.glPopMatrix();
				}
			}
			GL11.glPopMatrix();
			break;
		case FLUID:
			MonitoredList<MonitoredFluidStack> fluids = (MonitoredList<MonitoredFluidStack>) list;
			for (int i = perPage * pageCount; i < Math.min(perPage + perPage * pageCount, fluids.size()); i++) {
				MonitoredFluidStack fluid = fluids.get(i);
				FluidStack stack = fluid.getStoredStack().fluid;
				if (stack != null) {
					int current = i - perPage * pageCount;
					int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
					int yLevel = (int) (Math.floor((current / xSlots)));			
					GL11.glPushMatrix();
					GL11.glTranslated(xLevel, yLevel, 0);					
					GL11.glPushMatrix();
					GL11.glPushMatrix();
					GlStateManager.disableLighting();
					GL11.glTranslated(-1, -0.0625 * 12, +0.004);
					TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(stack.getFluid().getStill().toString());
					Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					InfoRenderer.renderProgressBarWithSprite(sprite, FLUID_DIMENSION, FLUID_DIMENSION, 0.012, fluid.getStored(), fluid.getStoredStack().capacity);
					GlStateManager.enableLighting();
					GL11.glTranslated(0, 0, -0.001);
					GL11.glPopMatrix();
					InfoRenderer.renderNormalInfo(container.display.getDisplayType(), FLUID_DIMENSION, FLUID_DIMENSION + 0.0625, 0.012, fluid.getClientIdentifier(), fluid.getClientObject());
					GL11.glPopMatrix();
					GL11.glPopMatrix();
				}
			}
			break;
		case ENERGY:
			MonitoredList<MonitoredEnergyStack> energy = (MonitoredList<MonitoredEnergyStack>) list;
			double spacing = 0.0625 * 7;
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
						isHighlighted = true;
					}
				}
				Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(2));
				InfoRenderer.renderProgressBar(width, 6 * 0.0625, scale, l, width);
				RenderHelper.restoreBlendState();
				GL11.glTranslated(0, 0, -0.00625);
				// GL11.glTranslated((width/2)-1, +1 + 0.0625 * 4, 0.00);
				GL11.glTranslated(1, 1 - 0.0625 * 3.5, 0.00);
				InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, 0.0625 * 6, scale / 3, isHighlighted ? new CustomColour(20, 100, 180).getRGB() : -1, Lists.newArrayList(info.coords.getMonitoredInfo().getClientIdentifier() + " - " + info.coords.getMonitoredInfo().getClientObject(), info.getClientIdentifier() + " - " + info.getClientObject()));
				GL11.glPopMatrix();
			}
			break;
		default:
			break;

		}
		//System.out.println(SimpleProfiler.finish("render") / 10000.0);

	}

	@Override
	public NBTTagCompound onClientClick(ScreenInteractionEvent event, IDisplayInfo renderInfo, EntityPlayer player, ItemStack stack, InfoContainer container) {
		NBTTagCompound clickTag = new NBTTagCompound();
		if (event.type == BlockInteractionType.SHIFT_RIGHT) {
			MonitoredList<?> list = getCachedList(renderInfo.getInfoUUID());
			if (list.size() > perPage * (pageCount + 1)) {
				this.pageCount++;
			} else {
				this.pageCount = 0;
			}
			player.addChatComponentMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "PAGE " + (pageCount + 1) + " of " + Math.min(pageCount + 1, Math.round((double) list.size() / Math.max(perPage, 1)))));
			return clickTag;
		}
		/* if (displayMenu) { return clickTag; } */
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
						player.addChatComponentMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "'" + energyStack.coords.getMonitoredInfo().getClientIdentifier() + "'" + " has been highlighted"));

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

	/* @SideOnly(Side.CLIENT) public void getButtons(ArrayList<DisplayButton> buttons) { super.getButtons(buttons); buttons.add(new DisplayButton("nxPg", 16, 16, "Next Page")); buttons.add(new DisplayButton("pvPg", 16, 16, "Previous Page")); } */
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