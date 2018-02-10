package sonar.logistics.client.gsi.info;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.CustomColour;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.client.gsi.GSIHelper;
import sonar.logistics.client.gsi.IGSIListViewer;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredEnergyStack;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;

public class GSILogicList extends AbstractGSI<LogicInfoList> implements IGSIListViewer {

	public int pageCount = 0;
	public int xSlots, ySlots, perPage = 0;
	public List<?> cachedList = null;
	public Type type;


	public enum Type {
		NONE, ITEM, FLUID, ENERGY;
	}

	public void setCachedList(AbstractChangeableList list, InfoUUID id) {
		cachedList = list.createSaveableList();
	}
	
	public void resetType(LogicInfoList info){
		type = null;
		getType(info);
	}
	public Type getType(LogicInfoList info) {
		if (type == null) {
			String infoID = info.infoID.getObject();
			if (infoID == null || infoID.isEmpty()) {
				return Type.NONE;
			}
			if (infoID.equals(MonitoredItemStack.id)) {
				type = Type.ITEM;
			} else if (infoID.equals(MonitoredFluidStack.id)) {
				type = Type.FLUID;
			} else if (infoID.equals(MonitoredEnergyStack.id)) {
				type = Type.ENERGY;
			}
		}
		return type;
	}

	public List<?> getCachedList(LogicInfoList info, InfoUUID id) {
		if (cachedList == null || info.listChanged) {
			info.listChanged = false;
			AbstractChangeableList<?> list = PL2.getInfoManager(true).getMonitoredList(id);
			cachedList = list != null ? list.createSaveableList() : Lists.newArrayList();			
			resetType(info);
			if (cachedList.size() < perPage * pageCount - 1) {
				pageCount = 0;
			}
		}
		return cachedList;
	}

	@Override
	public void renderGSIForeground(LogicInfoList info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		cachedList = getCachedList(info, displayInfo.getInfoUUID());
		if (cachedList == null || cachedList.isEmpty()) {
			return;
		}
		switch (type) {
		case ITEM:
			xSlots = (int) Math.ceil(width * 2);
			ySlots = (int) (Math.round(height * 2));
			perPage = xSlots * ySlots;
			InfoRenderer.renderInventory((List<MonitoredItemStack>) cachedList, perPage * pageCount, Math.min(perPage + perPage * pageCount, cachedList.size()), xSlots, ySlots);
			break;
		case FLUID:
			xSlots = (int) Math.round(width);
			ySlots = Math.max(1, (int) (Math.round(height)));
			perPage = xSlots * ySlots;
			List<MonitoredFluidStack> fluids = (List<MonitoredFluidStack>) cachedList;
			int start = perPage * pageCount;
			int finish = Math.min(perPage + (perPage * pageCount), fluids.size());
			double fluidWidth = Math.min(width, InfoRenderer.FLUID_DIMENSION);
			double fluidHeight = Math.min(height, InfoRenderer.FLUID_DIMENSION);
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
					InfoRenderer.renderProgressBarWithSprite(sprite, fluidWidth, fluidHeight, fluid.getStored(), fluid.getStoredStack().capacity);
					GlStateManager.enableLighting();
					GL11.glTranslated(0, 0, -0.001);
					GL11.glPopMatrix();
					InfoRenderer.renderNormalInfo(fluidWidth, fluidHeight + 0.0625 / 2, container.scale, fluid.getClientIdentifier(), fluid.getClientObject());
					GL11.glPopMatrix();
					GL11.glPopMatrix();
				}
			}
			break;
		case ENERGY:
			xSlots = (int) 1;
			ySlots = (int) (height / (0.0625 * 6));
			perPage = xSlots * ySlots;
			List<MonitoredEnergyStack> energys = (List<MonitoredEnergyStack>) cachedList;
			double spacing = 0.0625 * 7;
			GL11.glTranslated(-1, -1 + 0.0625 * 4, 0.00);
			int end = Math.min(perPage + perPage * pageCount, energys.size());
			for (int i = perPage * pageCount; i < end; i++) {
				MonitoredEnergyStack energy = energys.get(i);
				int current = i - perPage * pageCount;
				int xLevel = (int) (current - ((Math.floor((current / xSlots))) * xSlots));
				int yLevel = (int) (Math.floor((current / xSlots)));
				GL11.glPushMatrix();
				GL11.glTranslated(xLevel * spacing, yLevel * spacing, 0);
				double l = ((double) energy.getEnergyStack().stored * (double) (width) / energy.getEnergyStack().capacity);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GlStateManager.disableLighting();
				boolean isHighlighted = false;
				if (!RenderBlockSelection.positions.isEmpty()) {
					if (RenderBlockSelection.isPositionRenderered(energy.getMonitoredCoords().getCoords())) {
						isHighlighted = true;
					}
				}
				Minecraft.getMinecraft().getTextureManager().bindTexture(InfoContainer.getColour(2));
				InfoRenderer.renderProgressBar(width, 6 * 0.0625, l, width);
				GL11.glTranslated(0, 0, -0.00625);
				// GL11.glTranslated((width/2)-1, +1 + 0.0625 * 4, 0.00);
				GL11.glTranslated(1, 1 - 0.0625 * 3.5, 0.00);
				InfoRenderer.renderNormalInfo(width, 0.0625 * 6, scale / 3, isHighlighted ? new CustomColour(20, 100, 180).getRGB() : -1, Lists.newArrayList(energy.getMonitoredCoords().getClientIdentifier() + " - " + energy.getMonitoredCoords().getClientObject(), info.getClientIdentifier() + " - " + info.getClientObject()));
				GL11.glPopMatrix();
			}
			break;
		default:
			break;

		}
	}

	public NBTTagCompound createClickPacket(LogicInfoList info, DisplayScreenClick click, EnumHand hand) {
		List<?> list = getCachedList(info, renderInfo.getInfoUUID());
		switch(type){
		case ENERGY:
			int slot = (int) ((perPage * pageCount) + InteractionHelper.getListSlot(click, renderInfo, 0.0625 * 6, 0.0625 * 1, perPage));
			if (slot >= 0 && slot < list.size()) {
				MonitoredEnergyStack energyStack = (MonitoredEnergyStack) list.get(slot);
				if (click.type == BlockInteractionType.RIGHT) {
					RenderBlockSelection.addPosition(energyStack.getMonitoredCoords().getCoords(), false);
					player.sendMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "'" + energyStack.getMonitoredCoords().getClientIdentifier() + "'" + " has been highlighted"));
				}
			}
			break;
		case FLUID:
			slot = (perPage * pageCount) + InteractionHelper.getSlot(click, renderInfo, 1, 1);
			StoredFluidStack fluid = slot >= 0 && slot < list.size() ? ((MonitoredFluidStack) list.get(slot)).getStoredStack() : null;
			return GSIHelper.createFluidClickPacket(fluid, info.networkID.getObject());
		case ITEM:
			slot = (perPage * pageCount) + InteractionHelper.getSlot(click, renderInfo, 2, 2);
			StoredItemStack item = slot >= 0 && slot < list.size() ? ((MonitoredItemStack) list.get(slot)).getStoredStack() : null;
			return GSIHelper.createItemClickPacket(item, info.networkID.getObject());
		case NONE:
			break;
		default:
			break;
		
		}
		return new NBTTagCompound();
	}

	@Override
	public void onGSIClicked(LogicInfoList info, DisplayScreenClick click, EnumHand hand) {
		super.onGSIClicked(info, click, hand);
		if (click.type == BlockInteractionType.SHIFT_RIGHT) {
			List<?> list = getCachedList(info, renderInfo.getInfoUUID());
			if (list.size() > perPage * (pageCount + 1)) {
				this.pageCount++;
			} else {
				this.pageCount = 0;
			}
			int currentPage = (pageCount + 1);
			int totalPages = (int) (Math.round((double) list.size() / Math.max(perPage, 1)));
			player.sendMessage(new TextComponentTranslation(TextFormatting.BLUE + "Logistics: " + TextFormatting.RESET + "PAGE " + currentPage + " of " + totalPages));

		} else {
			sendGSIPacket(createClickPacket(info, click, hand), info, click, hand);
		}
	}

}
