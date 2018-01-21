package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.util.ITooltipFlag;
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
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.FlexibleGuiHandler;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.utils.ByteBufWritable;
import sonar.core.utils.IWorldPosition;
import sonar.core.utils.SortingDirection;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.readers.FluidReader;
import sonar.logistics.api.tiles.readers.IWirelessStorageReader;
import sonar.logistics.api.tiles.readers.InventoryReader;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.gui.generic.GuiSelectionGrid;
import sonar.logistics.common.containers.ContainerStorageViewer;
import sonar.logistics.common.multiparts.wireless.TileDataEmitter;
import sonar.logistics.helpers.ItemHelper;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.packets.PacketWirelessStorage;

public class GuiWirelessStorageReader extends GuiSelectionGrid<IInfo> {

	public static final ResourceLocation sorting_icons = new ResourceLocation(PL2Constants.MODID + ":textures/gui/sorting_icons.png");

	private SonarTextField searchField;
	public EntityPlayer player;
	public int identity;
	public int networkID;
	public ItemStack reader;

	public static boolean items = true;
	public static SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 0).addSyncType(SyncType.SPECIAL);
	public static SyncEnum<InventoryReader.SortingType> sortItems = (SyncEnum) new SyncEnum(InventoryReader.SortingType.values(), 0).addSyncType(SyncType.SPECIAL);
	public SyncEnum<FluidReader.SortingType> sortfluids = (SyncEnum) new SyncEnum(FluidReader.SortingType.values(), 0).addSyncType(SyncType.SPECIAL);

	public GuiWirelessStorageReader(ItemStack reader, int identity, int networkID, EntityPlayer player) {
		super(new ContainerStorageViewer(identity, player), (IWorldPosition) null);
		this.reader = reader;
		this.identity = identity;
		this.networkID = networkID;
		this.player = player;
	}

	public void initGui() {
		super.initGui();
		initButtons();
		searchField = new SonarTextField(1, this.fontRenderer, 135, 10, 104, 14);
		searchField.setMaxStringLength(20);
		fieldList.add(searchField);
	}

	public void initButtons() {
		super.initButtons();
		int start = 8;
		this.buttonList.add(new LogisticsButton.CHANNELS(this, 2, guiLeft + start, guiTop + 9));
		this.buttonList.add(new LogisticsButton.HELP(this, 5, guiLeft + start + 18 * 1, guiTop + 9));
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + xSize - 168 + 18, guiTop + 9, 32, 16 * sortingOrder.getObject().ordinal(), PL2Translate.BUTTON_SORTING_ORDER.t(), ""));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + xSize - 168 + 18 * 2, guiTop + 9, 64 + 48, 16 * sortItems.getObject().ordinal(), sortItems.getObject().getClientName(), ""));
		this.buttonList.add(new LogisticsButton(this, 3, guiLeft + 203, guiTop + 174, 32, 0, PL2Translate.BUTTON_DUMP_PLAYER.t(), ""));
		this.buttonList.add(new LogisticsButton(this, 4, guiLeft + 203 + 18, guiTop + 174, 32, 16, PL2Translate.BUTTON_DUMP_NETWORK.t(), ""));
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
				PL2.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {
					@Override
					public void writeToBuf(ByteBuf buf) {
						buf.writeBoolean(false);
						buf.writeInt(3);
					}
				}));
				break;
			case 4:
				PL2.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {
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
	public List<IInfo> getGridList() {
		String search = searchField.getText();
		if (items) {
			AbstractChangeableList<MonitoredItemStack> currentList = PL2.getClientManager().getMonitoredList(new InfoUUID(identity, TileDataEmitter.STATIC_ITEM_ID));
			if(currentList==null){
				return Lists.newArrayList();
			}
			ItemHelper.sortItemList(currentList, sortingOrder.getObject(), sortItems.getObject());
			if (search == null || search.isEmpty() || search.equals(" ")) {
				List<IInfo> infolist = Lists.newArrayList(currentList.createSaveableList());
				return infolist;
			} else {
				List<IInfo> searchlist = Lists.newArrayList();
				List<MonitoredItemStack> cached = currentList.createSaveableList();
				for (MonitoredItemStack stack : cached) {
					StoredItemStack item = stack.getStoredStack();
					if (stack != null && item != null && item.item.getDisplayName().toLowerCase().contains(search.toLowerCase())) {
						searchlist.add(stack);
					}
				}
				return searchlist;
			}
		} else {
			return PL2.getClientManager().getMonitoredList(new InfoUUID(identity, TileDataEmitter.STATIC_FLUID_ID)).createSaveableList();
		}
	}

	@Override
	public void onGridClicked(IInfo info, int pos, int button, boolean empty) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			button = 2;
		}
		final int usedButton = button;
		if (!empty) {
			PL2.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {
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
			PL2.network.sendToServer(new PacketWirelessStorage((IWirelessStorageReader) reader.getItem(), reader, player, 0, new ByteBufWritable(false) {
				@Override
				public void writeToBuf(ByteBuf buf) {
					buf.writeBoolean(false);
					buf.writeInt(usedButton);
				}
			}));
		}
	}

	@Override
	public void renderStrings(int x, int y) {
	}

	@Override
	public void renderGridElement(IInfo info, int x, int y, int slot) {
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
	public void renderElementToolTip(IInfo info, int x, int y) {
		if (info instanceof MonitoredItemStack) {
			MonitoredItemStack selection = (MonitoredItemStack) info;
			StoredItemStack storedStack = selection.getStoredStack();
			if (storedStack == null) {
				return;
			}
			List list = storedStack.item.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
			list.add(1, PL2Translate.BUTTON_STORED.t() + ": " + storedStack.stored);
			for (int k = 0; k < list.size(); ++k) {
				if (k == 0) {
					list.set(k, storedStack.item.getRarity().rarityColor + (String) list.get(k));
				} else {
					list.set(k, TextFormatting.GRAY + (String) list.get(k));
				}
			}

			FontRenderer font = storedStack.item.getItem().getFontRenderer(storedStack.item);
			drawHoveringText(list, x, y, (font == null ? fontRenderer : font));
		}
	}

}
