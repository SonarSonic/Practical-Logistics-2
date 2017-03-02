package sonar.logistics.client.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.SonarButtons.AnimatedButton;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.FlexibleGuiHandler;
import sonar.core.utils.CustomColour;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.Logistics;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.filters.BaseFilter;
import sonar.logistics.api.filters.FilterList;
import sonar.logistics.api.filters.ListPacket;
import sonar.logistics.api.filters.FluidFilter;
import sonar.logistics.api.filters.IFilteredTile;
import sonar.logistics.api.filters.IItemFilter;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.filters.ItemFilter;
import sonar.logistics.api.filters.OreDictFilter;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.nodes.TransferType;
import sonar.logistics.client.GuiHelpOverlay;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.GuiDisplayScreen.GuiState;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerFilterList;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.network.PacketNodeFilter;

public class GuiFilterList extends GuiSelectionList {
	public IFilteredTile tile;
	public int channelID;
	public GuiState state = GuiState.LIST;
	public int coolDown = 0;
	public INodeFilter lastFilter;
	public INodeFilter currentFilter;
	public EntityPlayer player;
	public GuiTextField oreDictField;
	public static final ResourceLocation filterButtons = new ResourceLocation(Logistics.MODID + ":textures/gui/filter_buttons.png");

	public enum GuiState {
		LIST(176, 166, 11, true), ITEM_FILTER(100, 100, 16, true), ORE_FILTER(100, 100, 11, true), FLUID_FILTER(100, 100, 16, true);

		int xSize, ySize;
		boolean hasScroller;
		int listSize;

		GuiState(int xSize, int ySize, int listSize, boolean hasScroller) {
			this.xSize = xSize;
			this.ySize = ySize;
			this.hasScroller = hasScroller;
		}
	}

	public GuiFilterList(EntityPlayer player, IFilteredTile tile, int channelID) {
		super(new ContainerFilterList(player, tile), tile);
		this.tile = tile;
		this.player = player;
		this.channelID = channelID;
		listHeight = 32;
		this.xSize = 182 + 66;
		this.ySize = state.ySize;
	}

	public void initGui() {
		super.initGui();
		switch (state) {
		case FLUID_FILTER:
			this.buttonList.add(new GuiButton(-1, guiLeft + 6, guiTop + 6, 60, 20, currentFilter.getTransferMode().name()));
			this.buttonList.add(new FluidFilterButton(this, 0, guiLeft + 70, guiTop + 8));
			break;
		case ITEM_FILTER:
			this.buttonList.add(new GuiButton(-1, guiLeft + 6, guiTop + 6, 60, 20, currentFilter.getTransferMode().name()));

			for (int i = 0; i < 5; i++) {
				this.buttonList.add(new ItemFilterButton(this, i, guiLeft + 70 + ((i) * 20), guiTop + 8));
			}

			break;
		case LIST:
			int start = 7;
			int gap = 18;
			this.buttonList.add(new LogisticsButton(this, -1, guiLeft + start, guiTop + 6, 64+16, 16*tile.getTransferMode().ordinal(), "TransferMode: " + tile.getTransferMode(), "button.TransferMode"));
			this.buttonList.add(new LogisticsButton(this, 0, guiLeft + start + gap, guiTop + 6, 32, 48, "New Item Filter", "button.ItemFilter"));
			this.buttonList.add(new LogisticsButton(this, 1, guiLeft + start + gap * 2, guiTop + 6, 32, 80, "New Ore Dict Filter", "button.OreFilter"));
			this.buttonList.add(new LogisticsButton(this, 2, guiLeft + start + gap * 3, guiTop + 6, 32, 64, "New Fluid Filter", "button.FluidFilter"));
			this.buttonList.add(new LogisticsButton(this, 3, guiLeft + start + gap * 4, guiTop + 6, 32, 0, "Move Up", "button.MoveUpFilter"));
			this.buttonList.add(new LogisticsButton(this, 4, guiLeft + start + gap * 5, guiTop + 6, 32, 16, "Move Down", "button.MoveDownFilter"));
			this.buttonList.add(new LogisticsButton(this, 5, guiLeft + start + gap * 6, guiTop + 6, 32, 32, "Delete", "button.DeleteFilter"));
			this.buttonList.add(new LogisticsButton(this, 6, guiLeft + start + gap * 7, guiTop + 6, 32, 96, "Clear All", "button.ClearAllFilter"));
			this.buttonList.add(new LogisticsButton(this, 7, guiLeft + start + gap * 8, guiTop + 6, 32, 96 + 16, "Channels", "button.Channels"));
			boolean itemTransfer = tile.isTransferEnabled(TransferType.ITEMS);
			this.buttonList.add(new LogisticsButton(this, 8, guiLeft + start + gap * 9, guiTop + 6, itemTransfer ? 16 : 0, 80, "Item Transfer: " + itemTransfer, "button.ItemTransfer"));
			boolean fluidTransfer = tile.isTransferEnabled(TransferType.FLUID);
			this.buttonList.add(new LogisticsButton(this, 9, guiLeft + start + gap * 10, guiTop + 6, fluidTransfer ? 16 : 0, 96, "Fluid Transfer: " + fluidTransfer, "button.FluidTransfer"));
			boolean energyTransfer = tile.isTransferEnabled(TransferType.ENERGY);
			this.buttonList.add(new LogisticsButton(this, 10, guiLeft + start + gap * 11, guiTop + 6, energyTransfer ? 16 : 0, 96 + 16, "Energy Transfer: " + energyTransfer, "button.EnergyTransfer"));
			this.buttonList.add(new LogisticsButton(this, 11, guiLeft + start + gap * 12, guiTop + 6, 32, 160 + 32 + (GuiHelpOverlay.enableHelp ? 16 : 0), "Help Enabled: " + GuiHelpOverlay.enableHelp, "button.HelpButton"));

			break;
		case ORE_FILTER:
			this.buttonList.add(new GuiButton(-1, guiLeft + 6, guiTop + 6, 60, 20, currentFilter.getTransferMode().name()));
			this.buttonList.add(new FluidFilterButton(this, 0, guiLeft + 70, guiTop + 8));
			this.buttonList.add(new GuiButton(1, guiLeft + 184, guiTop + 6, 20, 20, "+"));
			Keyboard.enableRepeatEvents(true);
			oreDictField = new GuiTextField(0, this.fontRendererObj, 90, 8, 90, 16);
			oreDictField.setMaxStringLength(20);
			oreDictField.setText("");
			// this.buttonList.add(new SideButton(12, guiLeft + 6, guiTop + 6, 34, 12, "DONE"));
			break;
		default:
			break;
		}
	}

	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		if (coolDown != 0) {
			coolDown--;
		}
	}

	public int ySize() {
		return 256;
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		switch (state) {
		case FLUID_FILTER:
			FluidFilter fluidFilter = (FluidFilter) currentFilter;
			switch (button.id) {
			case -1:
				fluidFilter.transferMode.incrementEnum();
				reset();
				break;
			}
			break;
		case ITEM_FILTER:
			ItemFilter filter = (ItemFilter) currentFilter;
			switch (button.id) {
			case -1:
				filter.transferMode.incrementEnum();
				reset();
				break;
			case 0:
				filter.matchNBT.invert();
				reset();
				break;
			case 1:
				filter.matchOreDict.invert();
				reset();
				break;
			case 2:
				filter.ignoreDamage.invert();
				reset();
				break;
			case 3:
				filter.matchModid.invert();
				reset();
				break;
			case 4:
				filter.listType.incrementEnum();
				reset();
				break;
			}
			break;
		case LIST:
			switch (button.id) {
			case -1:
				tile.incrementTransferMode();
				reset();
				break;
			case 0:
				filter = new ItemFilter();
				tile.getFilters().addObject(filter);
				lastFilter = filter;
				currentFilter = filter;
				this.changeState(GuiState.ITEM_FILTER);
				break;

			case 1:
				OreDictFilter orefilter = new OreDictFilter();
				tile.getFilters().addObject(orefilter);
				lastFilter = orefilter;
				currentFilter = orefilter;
				this.changeState(GuiState.ORE_FILTER);
				break;

			case 2:
				fluidFilter = new FluidFilter();
				tile.getFilters().addObject(fluidFilter);
				lastFilter = fluidFilter;
				currentFilter = fluidFilter;
				this.changeState(GuiState.FLUID_FILTER);
				break;
			case 3:
				if (currentFilter != null) {
					Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), ListPacket.MOVE_UP, currentFilter));
				}
				break;
			case 4:
				if (currentFilter != null) {
					Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), ListPacket.MOVE_DOWN, currentFilter));
				}
				break;
			case 5:
				if (currentFilter != null) {
					Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), ListPacket.REMOVE, currentFilter));
				}
				break;
			case 6:
				Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), ListPacket.CLEAR));
				break;
			case 7:
				if (tile instanceof IFlexibleGui) {
					FlexibleGuiHandler.changeGui((IFlexibleGui) tile, 1, 0, player.getEntityWorld(), player);
				}
				break;
			case 8:
				tile.setTransferType(TransferType.ITEMS, !tile.isTransferEnabled(TransferType.ITEMS));
				reset();
				break;
			case 9:
				tile.setTransferType(TransferType.FLUID, !tile.isTransferEnabled(TransferType.FLUID));
				reset();
				break;
			case 10:
				tile.setTransferType(TransferType.ENERGY, !tile.isTransferEnabled(TransferType.ENERGY));
				reset();
				break;
			case 11:
				GuiHelpOverlay.enableHelp = !GuiHelpOverlay.enableHelp;
				reset();
				break;
			}

			break;
		case ORE_FILTER:
			OreDictFilter oreFilter = (OreDictFilter) currentFilter;
			switch (button.id) {
			case -1:
				oreFilter.transferMode.incrementEnum();
				reset();
				break;
			case 0:
				oreFilter.listType.incrementEnum();
				reset();
				break;
			case 1:
				if (!oreDictField.getText().isEmpty()) {
					((OreDictFilter) currentFilter).addOreDict(oreDictField.getText());
					oreDictField.setText("");
				}
			}

			break;
		default:
			break;

		}
	}

	public void changeState(GuiState state) {
		if (state == GuiState.LIST && currentFilter != null) {
			Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), ListPacket.ADD, currentFilter));
		}
		this.state = state;
		this.xSize = 182 + 66;
		this.ySize = state.ySize;
		this.enableListRendering = state == GuiState.LIST || state == GuiState.ORE_FILTER;
		if (scroller != null)
			this.scroller.renderScroller = state.hasScroller;

		coolDown = state != GuiState.LIST ? 25 : 0;
		this.reset();
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		switch (state) {
		case FLUID_FILTER:
		case ITEM_FILTER:
			// GL11.glEnable(GL11.GL_DEPTH_TEST);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			renderStrings(x, y);
			ArrayList list = (ArrayList) getGridList().clone();
			if (list != null && !list.isEmpty()) {
				int start = (int) (getGridSize(list) / 12 * scroller.getCurrentScroll());
				int i = start * 12;
				int finish = Math.min(i + (12 * 7), getGridSize(list));
				for (int Y = 0; Y < 7; Y++) {
					for (int X = 0; X < 12; X++) {
						if (i < finish) {
							Object selection = list.get(i);
							if (selection != null) {
								renderGridSelection(selection, X, Y);
							}
						}
						i++;
					}
				}
			}
			if (x - guiLeft >= 13 && x - guiLeft <= 13 + (12 * 18) && y - guiTop >= 32 && y - guiTop <= 32 + (7 * 18)) {
				int start = (int) (getGridSize(list) / 12 * scroller.getCurrentScroll());
				int X = (x - guiLeft - 13) / 18;
				int Y = (y - guiTop - 32) / 18;
				int i = (start * 12) + X + ((Y) * 12);

				if (list != null) {
					if (i < list.size()) {
						Object selection = list.get(i);
						if (selection != null) {

							// GL11.glDisable(GL11.GL_DEPTH_TEST);
							GL11.glDisable(GL11.GL_LIGHTING);
							this.renderToolTip(selection, x - guiLeft, y - guiTop);
							GL11.glEnable(GL11.GL_LIGHTING);
							// GL11.glEnable(GL11.GL_DEPTH_TEST);
							// net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();

						}
					}
				}
			}
			break;
		case LIST:
			// FontHelper.textCentre(FontHelper.translate("Filters"), xSize, 6, LogisticsColours.white_text);
			// FontHelper.textCentre(String.format("Select the filter you wish to edit"), xSize, 18, LogisticsColours.grey_text);
			break;
		case ORE_FILTER:
			oreDictField.drawTextBox();
			break;
		default:
			break;

		}
		super.drawGuiContainerForegroundLayer(x, y);
	}

	// list
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, Object info) {
		switch (state) {
		case FLUID_FILTER:
			break;
		case ITEM_FILTER:
			break;
		case LIST:
			INodeFilter selection = (INodeFilter) info;
			if (selection instanceof ItemFilter && this.player.inventory.getItemStack() != null) {
				ItemFilter filter = (ItemFilter) selection;
				filter.addItem(new StoredItemStack(this.player.inventory.getItemStack(), 1));
				Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), ListPacket.ADD, selection));
				return;
			}
			if (selection instanceof FluidFilter && this.player.inventory.getItemStack() != null) {
				addFluidToFilter(selection, this.player.inventory.getItemStack());
				Logistics.network.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), ListPacket.ADD, selection));
				return;
			}

			lastFilter = selection;
			currentFilter = selection;
			if (buttonID == 1) {
				if (selection instanceof ItemFilter) {
					changeState(GuiState.ITEM_FILTER);
				} else if (selection instanceof FluidFilter) {
					changeState(GuiState.FLUID_FILTER);
				} else if (selection instanceof OreDictFilter) {
					changeState(GuiState.ORE_FILTER);
				}
			}
			break;
		case ORE_FILTER:
			if (buttonID == 1) {
				((OreDictFilter) currentFilter).removeOreDict((String) info);
			}

			break;
		default:
			break;
		}
	}

	public void setInfo() {
		if (state == GuiState.LIST)
			infoList = (ArrayList<INodeFilter>) tile.getFilters().objs.clone();
		if (state == GuiState.ORE_FILTER)
			infoList = (ArrayList) ((OreDictFilter) currentFilter).getOreIDs().clone();
	}

	@Override
	public boolean isCategoryHeader(Object info) {
		if (state == GuiState.LIST) {
			if (currentFilter == null || info == null) {
				return false;
			}
			return info.hashCode() == currentFilter.hashCode();
		} else if (state == GuiState.ORE_FILTER) {

		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(Object info) {
		return false;
	}

	@Override
	public boolean isPairedInfo(Object info) {
		return false;
	}

	@Override
	public void renderInfo(Object info, int yPos) {
		if (state == GuiState.LIST) {
			((INodeFilter) info).renderInfoInList(this, yPos);
		} else if (state == GuiState.ORE_FILTER) {
			GlStateManager.scale(0.75, 0.75, 0.75);
			FontHelper.text("Ore Filter", 16, (int) ((yPos + 2) * 1 / 0.75), Color.white.getRGB());
			FontHelper.text("Type: " + info, 88, (int) ((yPos + 2) * 1 / 0.75), Color.white.getRGB());
			GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.translate(0, 12, 0);
			List<ItemStack> ores = OreDictionary.getOres((String) info);

			int yOffset = 0;
			for (int i = 0; i < Math.min(12, ores.size()); i++) {
				if (i == 12) {
					yOffset++;
				}
				ItemStack item = ores.get(i);
				RenderHelper.renderItem(this, 13 + i * 18, -2 + yPos, item);
				RenderHelper.renderStoredItemStackOverlay(item, 0, 13 + i * 18, -2 + yPos + yOffset * 18, null, true);
				RenderHelper.restoreBlendState();
			}
			GlStateManager.translate(0, -12, 0);
		}
	}

	@Override
	public void mouseClicked(int x, int y, int button) throws IOException {
		if (coolDown != 0) {
			return;
		}
		if (state == GuiState.ITEM_FILTER || state == GuiState.FLUID_FILTER) {
			if (button == 0 || button == 1) {
				ArrayList list = (ArrayList) getGridList().clone();
				if (x - guiLeft >= 13 && x - guiLeft <= 13 + (12 * 18) && y - guiTop >= 32 && y - guiTop <= 32 + (7 * 18)) {
					int start = (int) (getGridSize(list) / 12 * scroller.getCurrentScroll());
					int X = (x - guiLeft - 13) / 18;
					int Y = (y - guiTop - 32) / 18;
					int i = (start * 12) + (12 * Y) + X;
					if (i < getGridList().size()) {
						Object storedStack = getGridList().get(i);
						if (storedStack != null) {
							onGridClicked(storedStack, i, button, false);
							return;
						}
					}
					onGridClicked(null, i, button, true);
				}
			}
			Slot itemSlot = null;
			for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);

				if (this.isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y)) {
					itemSlot = slot;
					break;
				}
			}
			if (itemSlot != null) {
				ItemStack itemstack = itemSlot.getStack();
				if (itemstack != null) {
					if (state == GuiState.ITEM_FILTER) {
						((ItemFilter) currentFilter).addItem(new StoredItemStack(itemstack, 1));
					} else if (state == GuiState.FLUID_FILTER) {
						addFluidToFilter(currentFilter, itemstack);
					}
				}
			}
		}
		if (state == GuiState.ORE_FILTER) {
			oreDictField.mouseClicked(x - guiLeft, y - guiTop, button);
		}
		super.mouseClicked(x, y, button);
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (state == GuiState.ORE_FILTER) {
			if (oreDictField.isFocused()) {
				if (c == 13 || c == 27) {
					oreDictField.setFocused(false);
				} else {
					oreDictField.textboxKeyTyped(c, i);
					final String text = oreDictField.getText();
					setString((text.isEmpty() || text == "" || text == null) ? "" : text);
				}
				return;
			}
		}
		if (state != GuiState.LIST && (i == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(i))) {
			changeState(GuiState.LIST);
			return;
		} else {
			super.keyTyped(c, i);
		}
	}

	public void setString(String string) {

	}

	public void drawInfo(int pos, DisplayInfo info) {
		int width = 162;
		int height = 20;
		int left = 7;
		int top = 20 + ((height + 6) * pos);
		drawTransparentRect(left, top, left + width, top + height, LogisticsColours.layers[2].getRGB());
		drawTransparentRect(left + 1, top + 1, left - 1 + width, top - 1 + height, LogisticsColours.grey_base.getRGB());
		if (info == null)
			return;

		IMonitorInfo monitorInfo = info.getSidedCachedInfo(true);
		if (monitorInfo instanceof INameableInfo) {
			INameableInfo directInfo = (INameableInfo) monitorInfo;
			FontHelper.text(directInfo.getClientIdentifier(), 11, top + 6, LogisticsColours.white_text.getRGB());
		} else {
			FontHelper.text("NO DATA", 11, top + 6, LogisticsColours.white_text.getRGB());
		}
	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	// grid
	public void onGridClicked(Object selection, int pos, int button, boolean empty) {
		if (state == GuiState.ITEM_FILTER) {
			ItemFilter filter = (ItemFilter) currentFilter;
			if (button == 1 && selection != null) {
				filter.removeItem((StoredItemStack) selection);
			} else if (this.player.inventory.getItemStack() != null) {
				filter.addItem(new StoredItemStack(this.player.inventory.getItemStack(), 1));
				return;
			}

		}
		if (state == GuiState.FLUID_FILTER) {
			FluidFilter filter = (FluidFilter) currentFilter;
			if (button == 1 && selection != null) {
				filter.removeFluid((StoredFluidStack) selection);
			} else {
				addFluidToFilter(currentFilter, this.player.inventory.getItemStack());
				return;
			}

		}
	}

	public void addFluidToFilter(INodeFilter filter, ItemStack stack) {
		if (filter != null && stack != null && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
			IFluidTankProperties properties = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).getTankProperties()[0];
			if (properties.getContents() != null) {
				((FluidFilter) filter).addFluid(new StoredFluidStack(properties.getContents(), 1));
			}
		}
	}

	// grid
	public void renderGridSelection(Object selection, int x, int y) {
		if (selection instanceof StoredItemStack) {
			StoredItemStack stored = (StoredItemStack) selection;
			RenderHelper.saveBlendState();
			ItemStack stack = stored.item;
			RenderHelper.renderItem(this, 13 + (x * 18), 32 + (y * 18), stack);
			RenderHelper.renderStoredItemStackOverlay(stack, 0, 13 + (x * 18), 32 + (y * 18), null, true);
			RenderHelper.restoreBlendState();
		} else if (selection instanceof StoredFluidStack) {
			StoredFluidStack stored = (StoredFluidStack) selection;
			if (stored.fluid != null) {
				GL11.glPushMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(stored.fluid.getFluid().getStill().toString());
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				drawTexturedModalRect(13 + (x * 18), 32 + (y * 18), sprite, 16, 16);
				GL11.glPopMatrix();
			}
		}
	}

	public void renderStrings(int x, int y) {
	}

	public void renderToolTip(Object selection, int x, int y) {
	}

	public ArrayList getGridList() {
		if (state == GuiState.ITEM_FILTER) {
			return ((ItemFilter) currentFilter).list.objs;
		}
		if (state == GuiState.FLUID_FILTER) {
			return ((FluidFilter) currentFilter).list.objs;
		}
		return null;
	}

	public double listScale() {
		return 1;
	}

	public int listSize() {
		return (int) Math.floor((166 - 29) / listHeight);
	}

	// grid
	public int getGridSize(ArrayList list) {
		return getGridList() == null ? 0 : list.size();
	}

	private boolean needsScrollBars(ArrayList list) {
		if (getGridSize(list) <= (12 * 7))
			return false;
		return true;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		this.renderPlayerInventory(40, 173);

		if (state == GuiState.FLUID_FILTER || state == GuiState.ITEM_FILTER) {
			drawRect(guiLeft + 12, guiTop + 31, guiLeft + 228, guiTop + 157, LogisticsColours.grey_base.getRGB());
			drawRect(guiLeft + 13, guiTop + 32, guiLeft + 227, guiTop + 156, LogisticsColours.blue_overlay.getRGB());
		}
		drawTransparentRect(guiLeft + 12, guiTop + 170, guiLeft + xSize - 12, guiTop + 252, LogisticsColours.grey_base.getRGB());
		drawTransparentRect(guiLeft + 13, guiTop + 171, guiLeft + xSize - 13, guiTop + 251, LogisticsColours.blue_overlay.getRGB());
		RenderHelper.restoreBlendState();
	}

	// BUTTONS

	@SideOnly(Side.CLIENT)
	public class ItemFilterButton extends AnimatedButton {
		public GuiFilterList list;

		public ItemFilterButton(GuiFilterList list, int id, int x, int y) {
			super(id, x, y, filterButtons, 15, 15);
			this.list = list;
		}

		public void drawButtonForegroundLayer(int x, int y) {
			String text = "BUTTON TEXT";
			ItemFilter filter = (ItemFilter) list.currentFilter;
			switch (id) {
			case 0:
				text = (filter.matchNBT.getObject() ? TextFormatting.WHITE : TextFormatting.GRAY) + "Use NBT: " + filter.matchNBT;
				break;
			case 1:
				text = (filter.matchOreDict.getObject() ? TextFormatting.WHITE : TextFormatting.GRAY) + "Use OreDict: " + filter.matchOreDict;
				break;
			case 2:
				text = (filter.ignoreDamage.getObject() ? TextFormatting.WHITE : TextFormatting.GRAY) + "Ignore Damage: " + filter.ignoreDamage;
				break;
			case 3:
				text = (filter.matchModid.getObject() ? TextFormatting.WHITE : TextFormatting.GRAY) + "Use MODID: " + filter.matchModid;
				break;
			case 4:
				text = filter.listType.getObject().name();
				break;
			}

			drawCreativeTabHoveringText(text, x, y);
		}

		@Override
		public void onClicked() {
		}

		@Override
		public int getTextureX() {
			boolean secondary = false;
			ItemFilter filter = (ItemFilter) list.currentFilter;
			switch (id) {
			case 0:
				secondary = !filter.matchNBT.getObject();
				break;
			case 1:
				secondary = !filter.matchOreDict.getObject();
				break;
			case 2:
				secondary = !filter.ignoreDamage.getObject();
				break;
			case 3:
				secondary = !filter.matchModid.getObject();
				break;
			case 4:
				secondary = filter.listType.getObject() != FilterList.WHITELIST;
				break;
			}

			return !secondary ? 0 : 0 + 16;
		}

		@Override
		public int getTextureY() {
			return (id * 16);
		}

	}

	@SideOnly(Side.CLIENT)
	public class FluidFilterButton extends AnimatedButton {
		public GuiFilterList list;

		public FluidFilterButton(GuiFilterList list, int id, int x, int y) {
			super(id, x, y, filterButtons, 15, 15);
			this.list = list;
		}

		public void drawButtonForegroundLayer(int x, int y) {
			String text = "BUTTON TEXT";
			BaseFilter filter = (BaseFilter) list.currentFilter;
			switch (id) {
			case 0:
				text = filter.listType.getObject().name();
				break;
			}

			drawCreativeTabHoveringText(text, x, y);
		}

		@Override
		public void onClicked() {
		}

		@Override
		public int getTextureX() {
			boolean secondary = false;
			BaseFilter filter = (BaseFilter) list.currentFilter;
			switch (id) {
			case 0:
				secondary = filter.listType.getObject() != FilterList.WHITELIST;
				break;
			}

			return !secondary ? 0 : 0 + 16;
		}

		@Override
		public int getTextureY() {
			return (4 * 16);
		}

	}

}
