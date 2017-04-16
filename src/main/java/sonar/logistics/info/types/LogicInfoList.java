package sonar.logistics.info.types;

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
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.utils.CustomColour;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.info.IAdvancedClickableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.tiles.displays.DisplayInteractionEvent;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = LogicInfoList.id, modid = PL2Constants.MODID)
public class LogicInfoList extends BaseInfo<LogicInfoList> implements INameableInfo<LogicInfoList>, IAdvancedClickableInfo {

	public static final String ITEM_CLICK = "ITEM_CLICK";
	public static final String id = "logiclist";
	public final SyncTagType.INT networkID = (INT) new SyncTagType.INT(2).setDefault(-1);
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncTagType.STRING infoID = new SyncTagType.STRING(1);

	public MonitoredList<?> cachedList = null;
	public boolean listChanged = true, wasRefreshed = false;
	public int pageCount = 0;
	public int xSlots, ySlots, perPage = 0;

	// client rendering
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
		if (infoID == null || infoID.isEmpty()) {
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
		return identity.getObject().equals(info.identity.getObject());
	}

	@Override
	public boolean isMatchingInfo(LogicInfoList info) {
		return infoID.getObject().equals(info.infoID.getObject());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof LogicInfoList;
	}

	@Override
	public INetworkHandler getHandler() {
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
			cachedList = list == null ? MonitoredList.newMonitoredList(networkID.getObject()) : (MonitoredList<IInfo>) list.cloneInfo();
			setType();
			if (cachedList.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
		}
		return cachedList;
	}

	public void setCachedList(MonitoredList list, InfoUUID id) {
		cachedList = list;
	}
	
	
	@Override
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderInfo(container, displayInfo, width, height, scale, infoPos);
		cachedList = getCachedList(displayInfo.getInfoUUID());
		if (cachedList.isEmpty()) {
			InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale / 1.4, "Nothing to display", "Click to refresh");
			return;
		}
		switch (type) {
		case ITEM:
			xSlots = (int) Math.ceil(width * 2);
			ySlots = (int) (Math.round(height * 2));
			perPage = xSlots * ySlots;
			InfoRenderer.renderInventory((MonitoredList<MonitoredItemStack>) cachedList, perPage * pageCount, Math.min(perPage + perPage * pageCount, cachedList.size()), xSlots, ySlots);
			break;
		case FLUID:
			xSlots = (int) Math.round(width);
			ySlots = (int) (Math.round(height));
			perPage = xSlots * ySlots;			
			MonitoredList<MonitoredFluidStack> fluids = (MonitoredList<MonitoredFluidStack>) cachedList;
			int start = perPage * pageCount;
			int finish = Math.min(perPage + (perPage * pageCount), fluids.size());
			for (int i = start; i < finish; i++) {
				MonitoredFluidStack fluid = fluids.get(i);
				FluidStack stack = fluid.getStoredStack().fluid;
				if (stack != null) {
					int current = i - start;
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
					InfoRenderer.renderProgressBarWithSprite(sprite, InfoRenderer.FLUID_DIMENSION, InfoRenderer.FLUID_DIMENSION, 0.012, fluid.getStored(), fluid.getStoredStack().capacity);
					GlStateManager.enableLighting();
					GL11.glTranslated(0, 0, -0.001);
					GL11.glPopMatrix();
					InfoRenderer.renderNormalInfo(container.display.getDisplayType(), InfoRenderer.FLUID_DIMENSION, InfoRenderer.FLUID_DIMENSION + 0.0625, 0.012, fluid.getClientIdentifier(), fluid.getClientObject());
					GL11.glPopMatrix();
					GL11.glPopMatrix();
				}
			}
			break;
		case ENERGY:
			xSlots = (int) 1;
			ySlots = (int) ((Math.round(height)) / (0.0625 * 7));
			perPage = xSlots * ySlots;
			MonitoredList<MonitoredEnergyStack> energy = (MonitoredList<MonitoredEnergyStack>) cachedList;
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

	}

	@Override
	public NBTTagCompound onClientClick(DisplayInteractionEvent event, IDisplayInfo renderInfo, EntityPlayer player, ItemStack stack, InfoContainer container) {
		NBTTagCompound clickTag = new NBTTagCompound();
		if (event.type == BlockInteractionType.SHIFT_RIGHT) {
			MonitoredList<?> list = getCachedList(renderInfo.getInfoUUID());
			if (list.size() > perPage * (pageCount + 1)) {
				this.pageCount++;
			} else {
				this.pageCount = 0;
			}
			player.addChatComponentMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "PAGE " + (pageCount + 1) + " of " + (Math.round((double) list.size() / Math.max(perPage, 1)) + 1)));
			return clickTag;
		}
		/* if (displayMenu) { return clickTag; } */
		if (infoID.getObject().equals(MonitoredItemStack.id) && event.hit != null) {
			int slot = (perPage * pageCount) + CableHelper.getSlot(container.getDisplay(), renderInfo.getRenderProperties(), event.hit.hitVec, 2, 2);
			MonitoredList<?> list = getCachedList(renderInfo.getInfoUUID());
			if (list != null) {
				boolean hasItem = false;
				if (slot >= 0 && slot < list.size()) {
					MonitoredItemStack itemStack = (MonitoredItemStack) list.get(slot);
					if (itemStack != null) {
						itemStack.writeData(clickTag, SyncType.SAVE);
						hasItem = true;
					}
				}
				clickTag.setBoolean(ITEM_CLICK, hasItem);
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
	public void onClickEvent(InfoContainer container, IDisplayInfo displayInfo, DisplayInteractionEvent event, NBTTagCompound clickTag) {
		if (infoID.getObject().equals(MonitoredItemStack.id)) {
			MonitoredItemStack clicked = clickTag.getBoolean(ITEM_CLICK) ? NBTHelper.instanceNBTSyncable(MonitoredItemStack.class, clickTag) : null;
			InfoHelper.screenItemStackClicked(clicked == null ? null : clicked.getStoredStack(), networkID.getObject(), event.type, event.doubleClick, displayInfo.getRenderProperties(), event.player, event.hand, event.player.getHeldItem(event.hand), event.hit);

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