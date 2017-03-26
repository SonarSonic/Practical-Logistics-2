package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.input.Keyboard;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.RenderHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.FlexibleGuiHandler;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.utils.ByteBufWritable;
import sonar.core.utils.IWorldPosition;
import sonar.core.utils.SortingDirection;
import sonar.logistics.Logistics;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.readers.InventoryReader;
import sonar.logistics.api.readers.InventoryReader.Modes;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.api.readers.FluidReader;
import sonar.logistics.api.readers.IWirelessStorageReader;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.generic.GuiSelectionGrid;
import sonar.logistics.common.containers.ContainerInventoryReader;
import sonar.logistics.common.containers.ContainerStorageViewer;
import sonar.logistics.common.multiparts.DataEmitterPart;
import sonar.logistics.common.multiparts.InventoryReaderPart;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.ItemHelper;
import sonar.logistics.network.PacketInventoryReader;
import sonar.logistics.network.PacketWirelessStorage;

public class GuiWirelessStorageReader extends GuiSelectionGrid<IMonitorInfo> {

	public static final ResourceLocation sorting_icons = new ResourceLocation(Logistics.MODID + ":textures/gui/sorting_icons.png");

	private SonarTextField searchField;
	public EntityPlayer player;
	public UUID uuid;
	public int networkID;
	public ItemStack reader;

	public static boolean items = true;
	public static SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 0).addSyncType(SyncType.SPECIAL);
	public static SyncEnum<InventoryReader.SortingType> sortItems = (SyncEnum) new SyncEnum(InventoryReader.SortingType.values(), 0).addSyncType(SyncType.SPECIAL);
	public SyncEnum<FluidReader.SortingType> sortfluids = (SyncEnum) new SyncEnum(FluidReader.SortingType.values(), 0).addSyncType(SyncType.SPECIAL);

	public GuiWirelessStorageReader(ItemStack reader, UUID uuid, int networkID, EntityPlayer player) {
		super(new ContainerStorageViewer(uuid, player), (IWorldPosition) null);
		this.reader = reader;
		this.uuid = uuid;
		this.networkID = networkID;
		this.player = player;
	}

	public void initGui() {
		super.initGui();
		initButtons();
		searchField = new SonarTextField(1, this.fontRendererObj, 135, 10, 104, 14);
		searchField.setMaxStringLength(20);
		fieldList.add(searchField);
	}

	public void initButtons() {
		super.initButtons();
		int start = 8;
		this.buttonList.add(new LogisticsButton(this, 2, guiLeft + start, guiTop + 9, 32, 96 + 16, "Select Emitter", "button.Channels"));
		this.buttonList.add(new LogisticsButton(this, 5, guiLeft + start + 18 * 1, guiTop + 9, 32, 160 + 32 + (GuiHelpOverlay.enableHelp ? 16 : 0), "Help Enabled: " + GuiHelpOverlay.enableHelp, "button.HelpButton"));
		// this.buttonList.add(new LogisticsButton(this, -1, guiLeft + start + 18 * 2, guiTop + 9, 64 + 32, 16 * part.setting.getObject().ordinal(), "SelectNetwork", ""));
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + xSize - 168 + 18, guiTop + 9, 32, 16 * sortingOrder.getObject().ordinal(), "Sorting Order", ""));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + xSize - 168 + 18 * 2, guiTop + 9, 64 + 48, 16 * sortItems.getObject().ordinal(), sortItems.getObject().getClientName(), ""));
		this.buttonList.add(new LogisticsButton(this, 3, guiLeft + 203, guiTop + 174, 32, 0, "Dump Player Inventory", ""));
		this.buttonList.add(new LogisticsButton(this, 4, guiLeft + 203 + 18, guiTop + 174, 32, 16, "Dump Network", ""));
	}

	public void actionPerformed(GuiButton button) {
		if (button != null) {
			switch (button.id) {
			case 0:
				sortingOrder.incrementEnum();
				initButtons();
				break;
			case 1:
				if (items) {
					sortItems.incrementEnum();
				} else {
					sortfluids.incrementEnum();
				}
				initButtons();
				break;
			case 2:
				FlexibleGuiHandler.changeGui((IFlexibleGui) player.getHeldItemMainhand().getItem(), 1, 0, player.getEntityWorld(), player);
				break;
			case 3:
				Logistics.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {

					@Override
					public void writeToBuf(ByteBuf buf) {
						buf.writeBoolean(false);
						buf.writeInt(3);
					}

				}));
				break;
			case 4:
				Logistics.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {

					@Override
					public void writeToBuf(ByteBuf buf) {
						buf.writeBoolean(false);
						buf.writeInt(4);
					}

				}));
				break;
			case 5:
				GuiHelpOverlay.enableHelp = !GuiHelpOverlay.enableHelp;
				reset();
				break;
			}
		}
	}

	public void switchState() {
		SonarCore.refreshFlexibleContainer(player);
	}

	@Override
	public void mouseClicked(int i, int j, int k) throws IOException {
		super.mouseClicked(i, j, k);
		if (k == 1) {
			searchField.setText("");
		}
	}

	@Override
	public MonitoredList<IMonitorInfo> getGridList() {
		String search = searchField.getText();
		if (items) {
			MonitoredList<MonitoredItemStack> currentList = Logistics.getClientManager().getMonitoredList(networkID, new InfoUUID(uuid.hashCode(), DataEmitterPart.STATIC_ITEM_ID));
			ItemHelper.sortItemList(currentList, sortingOrder.getObject(), sortItems.getObject());

			MonitoredList<IMonitorInfo> list = MonitoredList.newMonitoredList(networkID);
			list.addAll(currentList);
			if (search == null || search.isEmpty() || search.equals(" ")) {
				return list;
			} else {
				MonitoredList<IMonitorInfo> searchList = MonitoredList.newMonitoredList(networkID);
				for (MonitoredItemStack stack : (ArrayList<MonitoredItemStack>) currentList.clone()) {
					StoredItemStack item = stack.getStoredStack();
					if (stack != null && item != null && item.item.getDisplayName().toLowerCase().contains(search.toLowerCase())) {
						searchList.add(stack);
					}
				}
				return searchList;
			}
		} else {
			return Logistics.getClientManager().getMonitoredList(networkID, new InfoUUID(uuid.hashCode(), DataEmitterPart.STATIC_FLUID_ID));
		}
	}

	@Override
	public void onGridClicked(IMonitorInfo info, int pos, int button, boolean empty) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			button = 2;
		}
		final int usedButton = button;
		if (!empty) {
			Logistics.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {

				@Override
				public void writeToBuf(ByteBuf buf) {
					MonitoredItemStack selection = (MonitoredItemStack) info;
					if (selection != null && selection.getStoredStack().item != null) {
						buf.writeBoolean(true);
						ByteBufUtils.writeItemStack(buf, selection.getStoredStack().item);
					} else {
						buf.writeBoolean(false);
					}
					buf.writeInt(usedButton);
				}

			}));

		} else {
			Logistics.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {

				@Override
				public void writeToBuf(ByteBuf buf) {
					buf.writeBoolean(false);
					buf.writeInt(usedButton);
				}

			}));
		}

		/* if (getSetting() == STACK) { handler.current = selection.item; handler.current.stackSize = 1; Logistics.network.sendToServer(new PacketInventoryReader(tile.xCoord, tile.yCoord, tile.zCoord, handler.current)); } if (getSetting() == POS) { List<StoredItemStack> currentList = (List<StoredItemStack>) ((ArrayList<StoredItemStack>) handler.stacks).clone(); int position = 0; for (StoredItemStack stack : currentList) { if (stack != null) { if (stack.equals(selection)) { String posString = String.valueOf(position); slotField.setText(posString); setPosSlot(posString); } } position++; } } */
	}

	@Override
	public void renderStrings(int x, int y) {
	}

	@Override
	public void renderSelection(IMonitorInfo info, int x, int y, int slot) {
		if (info instanceof MonitoredItemStack) {
			MonitoredItemStack selection = (MonitoredItemStack) info;
			RenderHelper.saveBlendState();
			StoredItemStack storedStack = selection.getStoredStack();
			if (storedStack == null) {
				return;
			}
			ItemStack stack = storedStack.item;
			RenderHelper.renderItem(this, 13 + (x * 18), 32 + (y * 18), stack);
			RenderHelper.renderStoredItemStackOverlay(stack, storedStack.stored, 13 + (x * 18), 32 + (y * 18), null, true);
			RenderHelper.restoreBlendState();
		}
	}

	@Override
	public void renderToolTip(IMonitorInfo info, int x, int y) {
		if (info instanceof MonitoredItemStack) {
			MonitoredItemStack selection = (MonitoredItemStack) info;
			StoredItemStack storedStack = selection.getStoredStack();
			if (storedStack == null) {
				return;
			}
			List list = storedStack.item.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
			list.add(1, "Stored: " + storedStack.stored);
			for (int k = 0; k < list.size(); ++k) {
				if (k == 0) {
					list.set(k, storedStack.item.getRarity().rarityColor + (String) list.get(k));
				} else {
					list.set(k, TextFormatting.GRAY + (String) list.get(k));
				}
			}

			FontRenderer font = storedStack.item.getItem().getFontRenderer(storedStack.item);
			drawHoveringText(list, x, y, (font == null ? fontRendererObj : font));
		}
	}

}
