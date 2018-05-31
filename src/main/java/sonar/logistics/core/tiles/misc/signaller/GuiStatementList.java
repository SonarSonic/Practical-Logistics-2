package sonar.logistics.core.tiles.misc.signaller;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import sonar.core.client.gui.GuiHelpOverlay;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.inventory.containers.ContainerMultipartSync;
import sonar.core.network.sync.ObjectType;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.core.tiles.displays.info.IComparableInfo;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.INameableInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.comparators.ComparableObject;
import sonar.logistics.api.core.tiles.displays.info.comparators.InputTypes;
import sonar.logistics.api.core.tiles.displays.info.comparators.LogicOperator;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.api.core.tiles.readers.INetworkReader;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.gui.GuiSelectionList;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.base.gui.buttons.LogisticsButton;
import sonar.logistics.base.gui.overlays.OverlayBlockSelection;
import sonar.logistics.base.utils.ListPacket;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.core.tiles.displays.info.InfoRenderHelper;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.items.MonitoredItemStack;
import sonar.logistics.network.packets.PacketEmitterStatement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiStatementList extends GuiSelectionList<Object> {

	public TileRedstoneSignaller tile;
	public GuiState state = GuiState.LIST;
	public int coolDown = 0;
	public RedstoneSignallerStatement currentFilter;
	public RedstoneSignallerStatement lastFilter;
	public int infoPos;
	public boolean currentBool = false;

	public SonarTextField inputField;

	public enum GuiState {
		LIST(true), STATEMENT(false), CHANNELS(true), STRING(true);

		boolean hasScroller;

		GuiState(boolean hasScroller) {
			this.hasScroller = hasScroller;
		}
	}

	public GuiStatementList(EntityPlayer player, TileRedstoneSignaller tileRedstoneSignaller) {
		super(new ContainerMultipartSync(tileRedstoneSignaller), tileRedstoneSignaller);
		this.tile = tileRedstoneSignaller;
		this.xSize = 182 + 66;
		this.ySize = ySize + 22;
	}

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.listHeight = GuiState.LIST == state ? 24 : 12;
		super.initGui();
		switch (state) {
		case LIST:
			int start = 42;
			this.buttonList.add(new LogisticsButton(this, 0, guiLeft + start, guiTop + 6, 48, 16 * tile.emitterMode().getObject().ordinal(), "Emit If: " + tile.emitterMode().getObject().name(), "button.EmitterMode"));
			this.buttonList.add(new LogisticsButton(this, 1, guiLeft + start + 20, guiTop + 6, 32, 128, "New Statement", "button.NewStatement"));
			this.buttonList.add(new LogisticsButton(this, 2, guiLeft + start + 20 * 2, guiTop + 6, 32, 0, PL2Translate.BUTTON_MOVE_UP.t(), "button.MoveUpStatement"));
			this.buttonList.add(new LogisticsButton(this, 3, guiLeft + start + 20 * 3, guiTop + 6, 32, 16, PL2Translate.BUTTON_MOVE_DOWN.t(), "button.MoveDownStatement"));
			this.buttonList.add(new LogisticsButton(this, 4, guiLeft + start + 20 * 4, guiTop + 6, 32, 32, PL2Translate.BUTTON_DELETE.t(), "button.DeleteStatement"));
			this.buttonList.add(new LogisticsButton(this, 5, guiLeft + start + 20 * 5, guiTop + 6, 32, 96, PL2Translate.BUTTON_CLEAR_ALL.t(), "button.ClearAllStatements"));
			this.buttonList.add(new LogisticsButton(this, 6, guiLeft + start + 20 * 6, guiTop + 6, 32, 128 + 16, "Refresh", "button.RedstoneSignallerRefresh"));
			this.buttonList.add(new LogisticsButton.HELP(this, 7, guiLeft + start + 20 * 7, guiTop + 6));
			break;
		case STATEMENT:
			this.buttonList.add(new GuiButton(0, guiLeft + xSize / 2 - 60, guiTop + 116, 120, 20, "Input Type: " + currentFilter.getInputType()));
			this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 6, guiTop + 16, 32, 48, "Info Source", "button.InfoSignallerSource"));
			this.buttonList.add(new LogisticsButton(this, 3, guiLeft + 6, guiTop + 16 + 20, 32, 80, "Object Selection", "button.ObjectSelectionSource"));
			if (currentFilter.getInputType().usesInfo()) {
				this.buttonList.add(new LogisticsButton(this, 2, guiLeft + 6, guiTop + 72, 32, 48, "Info Source", "button.InfoSignallerSource"));
				this.buttonList.add(new LogisticsButton(this, 4, guiLeft + 6, guiTop + 92, 32, 80, "Object Selection", "button.ObjectSelectionSource"));
			} else if (currentFilter.getInputType() == InputTypes.BOOLEAN) {
				this.buttonList.add(new GuiButton(8, guiLeft + xSize / 2 - 60, guiTop + 80, 120, 20, "" + currentBool));
			} else {
				Keyboard.enableRepeatEvents(true);
				inputField = new SonarTextField(0, this.fontRenderer, 8, 96, xSize - 16, 12);
				inputField.setDigitsOnly(currentFilter.getInputType() == InputTypes.NUMBER);
				inputField.setMaxStringLength(20);
				inputField.setText(currentFilter.obj.get() == null ? "" : currentFilter.obj.get().toString());
				fieldList.add(inputField);
			}
			this.buttonList.add(new GuiButton(5, guiLeft + xSize / 2 - 25, guiTop + 141, 50, 20, ((LogicOperator) currentFilter.operator.getObject()).operation));

			this.buttonList.add(new GuiButton(6, guiLeft + xSize / 2, guiTop + 164, xSize / 2 - 4, 20, "SAVE"));
			this.buttonList.add(new GuiButton(7, guiLeft + 4, guiTop + 164, xSize / 2 - 4, 20, "RESET"));

			break;
		default:
			break;
		}
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		switch (state) {
		case LIST:
			switch (button.id) {
			case 0:
				tile.mode.incrementEnum();
				tile.sendByteBufPacket(1);
				reset();
				break;
			case 1:
				RedstoneSignallerStatement statement = new RedstoneSignallerStatement();
				lastFilter = statement;
				currentFilter = statement;
				this.changeState(GuiState.STATEMENT);
				break;
			case 2:
				if (currentFilter != null) {
					PL2.network.sendToServer(new PacketEmitterStatement(tile.getSlotID(), tile.getCoords().getBlockPos(), ListPacket.MOVE_UP, currentFilter));
				}
				break;
			case 3:
				if (currentFilter != null) {
					PL2.network.sendToServer(new PacketEmitterStatement(tile.getSlotID(), tile.getCoords().getBlockPos(), ListPacket.MOVE_DOWN, currentFilter));
				}
				break;
			case 4:
				if (currentFilter != null) {
					PL2.network.sendToServer(new PacketEmitterStatement(tile.getSlotID(), tile.getCoords().getBlockPos(), ListPacket.REMOVE, currentFilter));
				}
				break;
			case 5:
				PL2.network.sendToServer(new PacketEmitterStatement(tile.getSlotID(), tile.getCoords().getBlockPos(), ListPacket.CLEAR));
				break;
			case 6:
				tile.requestSyncPacket();
				break;
			case 7:
				GuiHelpOverlay.enableHelp = !GuiHelpOverlay.enableHelp;
				reset();
				break;
			}
			break;
		case STATEMENT:
			switch (button.id) {
			case 0:
				currentFilter.incrementInputType();
				inputField.setText("");
				currentFilter.obj.set("", ObjectType.STRING);
				currentFilter.comparatorID.setObject(currentFilter.getInputType().comparatorID);
				if (!currentFilter.validOperators().contains(currentFilter.getOperator())) {
					currentFilter.operator.setObject((LogicOperator) currentFilter.validOperators().get(0));
				}
				if (currentFilter.getInputType() == InputTypes.BOOLEAN) {
					currentBool = false;
					currentFilter.obj.set(false, ObjectType.BOOLEAN);
				}
				reset();
				break;
			case 1:
			case 2:
				this.changeState(GuiState.CHANNELS);
				this.infoPos = button.id - 1;
				break;
			case 3:
			case 4:
				this.changeState(GuiState.STRING);
				this.infoPos = button.id - 3;
				break;
			case 5:
				currentFilter.incrementOperator();
				this.reset();
				break;
			case 6:
				PL2.network.sendToServer(new PacketEmitterStatement(tile.getSlotID(), tile.getCoords().getBlockPos(), ListPacket.ADD, currentFilter));
				this.changeState(GuiState.LIST);
				break;
			case 7:
				// DON'T CHANGE. RESET!
				this.changeState(GuiState.LIST);
				break;
			case 8:
				currentBool = !currentBool;
				currentFilter.obj.set(currentBool, ObjectType.BOOLEAN);
				reset();
				break;
			}
			break;
		default:
			break;
		}
	}

	public void changeState(GuiState state) {
		if (state == GuiState.LIST && currentFilter != null) {
			// Logistics.handling.sendToServer(new PacketNodeFilter(tile.getIdentity(), tile.getCoords().getBlockPos(), FilterPacket.ADD, currentFilter));
		}
		this.state = state;
		// this.xSize = 182 + 66;
		// this.ySize = state.ySize;
		this.enableListRendering = state.hasScroller;
		if (scroller != null)
			this.scroller.renderScroller = state.hasScroller;

		coolDown = state != GuiState.LIST ? 15 : 0;
		this.reset();
	}

	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		if (coolDown != 0) {
			coolDown--;
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		switch (state) {
		case CHANNELS:
			FontHelper.textCentre(FontHelper.translate("Info Selection"), xSize, 6, PL2Colours.white_text);
			FontHelper.textCentre("Select the Info you wish to check against", xSize, 18, PL2Colours.grey_text);
			break;
		case LIST:
			break;
		case STATEMENT:

			InfoUUID uuid1 = (InfoUUID) currentFilter.uuid1.getObject();
			InfoUUID uuid2 = (InfoUUID) currentFilter.uuid2.getObject();
			String info1 = "<- Select the info source", info2 = info1;
			String obj1 = "<- Select the object to compare", obj2 = obj1;
			boolean has1 = false, has2 = false;

			if (InfoUUID.valid(uuid1)) {
				IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(uuid1).copy();
				if (monitorInfo instanceof IComparableInfo) {
					info1 = monitorInfo.getID().toUpperCase() + " - " + monitorInfo.toString();
					ComparableObject obj = ComparableObject.getComparableObject(((IComparableInfo) monitorInfo).getComparableObjects(new ArrayList<>()), currentFilter.key1.getObject());
					if (obj != null) {
						has1 = true;
						obj1 = currentFilter.key1.getObject() + " - " + obj.object.toString();
					}
				}
			}
			if (currentFilter.getInputType().usesInfo()) {
				if (InfoUUID.valid(uuid2)) {
					IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(uuid2).copy();
					if (monitorInfo instanceof IComparableInfo) {
						info2 = monitorInfo.getID().toUpperCase() + " - " + monitorInfo.toString();
						ComparableObject obj = ComparableObject.getComparableObject(((IComparableInfo) monitorInfo).getComparableObjects(new ArrayList<>()), currentFilter.key2.getObject());
						if (obj != null) {
							has2 = true;
							obj2 = currentFilter.key2.getObject() + " - " + obj.object.toString();
						}
					}
				}
				FontHelper.text(info2, 26, 76, PL2Colours.white_text.getRGB());
				FontHelper.text(obj2, 26, 96, PL2Colours.white_text.getRGB());
			} else if (currentFilter.getInputType() == InputTypes.BOOLEAN) {
				has2 = true;
				obj2 = "" + currentBool;
			} else {
				FontHelper.text("Input Field", 12, 80, PL2Colours.white_text.getRGB());

				if (!inputField.getText().isEmpty()) {
					has2 = true;
					obj2 = inputField.getText();
				}
			}

			FontHelper.textOffsetCentre(has1 ? obj1 : "Info 1", 48, 148, PL2Colours.white_text);
			FontHelper.textOffsetCentre(has2 ? obj2 : "Info 2", xSize - 48, 148, PL2Colours.white_text);

			FontHelper.text(info1, 26, 20, PL2Colours.white_text.getRGB());
			FontHelper.text(obj1, 26, 40, PL2Colours.white_text.getRGB());

			FontHelper.textCentre("Info 1", xSize, 6, PL2Colours.white_text);
			FontHelper.textCentre("Info 2", xSize, 60, PL2Colours.white_text);

			break;
		case STRING:
			uuid1 = (InfoUUID) (infoPos == 0 ? currentFilter.uuid1.getObject() : currentFilter.uuid2.getObject());
			if (InfoUUID.valid(uuid1)) {
				IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(uuid1);
				if (monitorInfo != null) {
					FontHelper.textCentre("Info Type: " + monitorInfo.getID().toUpperCase(), xSize, 6, PL2Colours.white_text.getRGB());
					GlStateManager.scale(0.75, 0.75, 0.75);
					FontHelper.textCentre(monitorInfo.toString(), (int) (xSize * (1 / 0.75)), (int) (18 * (1 / 0.75)), PL2Colours.white_text.getRGB());
					GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);
				}
			}
			break;
		default:
			break;

		}
		super.drawGuiContainerForegroundLayer(x, y);
		// FontHelper.textCentre(FontHelper.translate("Channel Selection"), xSize, 6, PL2Colours.white_text);
		// FontHelper.textCentre(String.format("Select the channels you wish to monitor"), xSize, 18, PL2Colours.grey_text);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		if (state == GuiState.STATEMENT) {
			// this.drawHorizontalLine(guiLeft + 2, guiLeft + xSize - 3, guiTop + ySize / 2, PL2Colours.blue_overlay.getRGB());

			drawTransparentRect(this.guiLeft + 4, this.guiTop + 4, this.guiLeft + xSize - 4, this.guiTop + 56, PL2Colours.layers[2].getRGB());

			drawTransparentRect(this.guiLeft + 4, this.guiTop + 140, this.guiLeft + xSize - 4, this.guiTop + 162, PL2Colours.layers[1].getRGB());

			drawTransparentRect(this.guiLeft + 4, this.guiTop + 58, this.guiLeft + xSize - 4, this.guiTop + 138, PL2Colours.layers[2].getRGB());
		}


	}

	@Override
	public int getColour(int i, int type) {
		return PL2Colours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(Object info) {
		if (info instanceof INetworkReader) {
			if (!OverlayBlockSelection.positions.isEmpty()) {
                return OverlayBlockSelection.isPositionRenderered(((INetworkReader) info).getCoords());
			}
		}
		return false;
	}

	@Override
	public boolean isSelectedInfo(Object info) {
		switch (state) {
		case CHANNELS:
			if (info instanceof InfoUUID && currentFilter != null) {
				if (infoPos == 0)
					return currentFilter.uuid1.getObject() != null && currentFilter.uuid1.getObject().equals(info);
				else
					return currentFilter.uuid2.getObject() != null && currentFilter.uuid2.getObject().equals(info);
			}
			break;
		case LIST:
			break;
		case STATEMENT:
			break;
		case STRING:
			if (info instanceof ComparableObject && currentFilter != null) {
				if (infoPos == 0)
					return currentFilter.key1.getObject() != null && currentFilter.key1.getObject().equals(((ComparableObject) info).string);
				else
					return currentFilter.key2.getObject() != null && currentFilter.key2.getObject().equals(((ComparableObject) info).string);
			}
			break;
		default:
			break;

		}

		return false;
	}

	@Override
	public boolean isCategoryHeader(Object info) {
		if (info instanceof RedstoneSignallerStatement) {
			return info == currentFilter;
		}

		return info instanceof IInfoProvider;
	}

	@Override
	public void renderInfo(Object info, int yPos) {
		switch (state) {
		case CHANNELS:
			if (info instanceof InfoUUID) {
				IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(info);
				if (monitorInfo != null) {
					InfoRenderHelper.renderMonitorInfoInGUI(monitorInfo, yPos + 1, PL2Colours.white_text.getRGB());
				} else {

					FontHelper.text("-", InfoRenderHelper.left_offset, yPos, PL2Colours.white_text.getRGB());
				}
			} else if (info instanceof IInfoProvider) {
				IInfoProvider monitor = (IInfoProvider) info;
				InfoRenderHelper.renderMonitorInfoInGUI(new MonitoredBlockCoords(monitor.getCoords(), LogisticsHelper.getCoordItem(monitor.getCoords(), mc.world)), yPos + 1, PL2Colours.white_text.getRGB());
			}
			break;
		case LIST:
			if (info instanceof RedstoneSignallerStatement) {
				RedstoneSignallerStatement statement = (RedstoneSignallerStatement) info;
				String infoType1 = "INFO";
				String infoType2 = "INFO";
				String infoObj1 = "NULL";
				String infoObj2 = "NULL";
				if (statement.uuid1.getObject() != null) {
					Pair<String, String> infoStrings = getInfoTypeAndObjectStrings((InfoUUID) statement.uuid1.getObject(), statement.key1.getObject());
					infoType1 = infoStrings.a;
					infoObj1 = infoStrings.b;
				}
				if (statement.getInputType().usesInfo()) {
					if (statement.uuid2.getObject() != null) {
						Pair<String, String> infoStrings = getInfoTypeAndObjectStrings((InfoUUID) statement.uuid2.getObject(), statement.key2.getObject());
						infoType2 = infoStrings.a;
						infoObj2 = infoStrings.b;
					}
				} else {
					if (statement.getInputType() != null) {
						infoType2 = statement.getInputType().name();
					}
					if (statement.obj.get() != null) {
						infoObj2 = statement.obj.get().toString();
					}
				}
				FontHelper.text(infoType1 + ": " + TextFormatting.WHITE + TextFormatting.ITALIC + infoObj1 + " " + TextFormatting.DARK_AQUA + ((LogicOperator) statement.operator.getObject()).operation + TextFormatting.RESET + " " + infoType2 + ": " + TextFormatting.WHITE + TextFormatting.ITALIC + infoObj2, InfoRenderHelper.left_offset, yPos, PL2Colours.white_text.getRGB());
				FontHelper.text("Current State: " + (statement.wasTrue.getObject() ? TextFormatting.GREEN : TextFormatting.RED) + statement.wasTrue.getObject(), InfoRenderHelper.left_offset, yPos + 14, PL2Colours.white_text.getRGB());

			}
			break;
		case STATEMENT:
			break;
		case STRING:
			if (info instanceof ComparableObject) {
				ComparableObject comparable = (ComparableObject) info;
				FontHelper.text(comparable.string, InfoRenderHelper.left_offset, yPos, PL2Colours.white_text.getRGB());
				if (comparable.object != null) {
					if (comparable.object instanceof Item) {
						FontHelper.text(((MonitoredItemStack) comparable.source).getItemStack().getDisplayName(), InfoRenderHelper.middle_offset, yPos, PL2Colours.white_text.getRGB());
					} else {
						FontHelper.text(comparable.object.toString(), InfoRenderHelper.middle_offset, yPos, PL2Colours.white_text.getRGB());
					}
					FontHelper.text(ObjectType.getInfoType(comparable.object).toString().toLowerCase(), InfoRenderHelper.right_offset, yPos, PL2Colours.white_text.getRGB());

				} else {
					FontHelper.text("ERROR", InfoRenderHelper.middle_offset, yPos, PL2Colours.white_text.getRGB());
				}
			}
			break;
		default:
			break;
		}
	}

	public Pair<String, String> getInfoTypeAndObjectStrings(InfoUUID id, String key) {
		String infoType = "INFO", infoObj = "NULL";
		IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(id);
		if (monitorInfo instanceof INameableInfo) {
			infoType = ((INameableInfo) monitorInfo).getClientIdentifier();
		} else {
			infoType = monitorInfo != null ? monitorInfo.toString() : "INFO 1";
		}
		if (monitorInfo instanceof IComparableInfo && key != null) {
			List<ComparableObject> infoList = ((IComparableInfo) monitorInfo).getComparableObjects(new ArrayList<>());
			ComparableObject obj = ComparableObject.getComparableObject(infoList, key);
			if (obj != null && obj.object != null) {
				infoObj = obj.object.toString();
			}
		}
		return new Pair(infoType, infoObj);
	}

	@Override
	public void selectionPressed(GuiButton button, int pos, int buttonID, Object info) {
		switch (state) {
		case LIST:
			if (info instanceof RedstoneSignallerStatement) {
				RedstoneSignallerStatement statement = (RedstoneSignallerStatement) info;
				if (buttonID == 1) {
					this.currentFilter = statement;
					lastFilter = statement;
					this.changeState(GuiState.STATEMENT);
				} else {
					currentFilter = info != currentFilter ? statement : null;
				}
			}
			break;
		case STATEMENT:
			break;
		case CHANNELS:
			if (buttonID == 0 && info instanceof InfoUUID) {
				if (infoPos == 0) {
					currentFilter.uuid1.setObject((InfoUUID) info);
				} else {
					currentFilter.uuid2.setObject((InfoUUID) info);
				}
				/* part.container().setUUID((InfoUUID) info, infoID); part.currentSelected = infoID; tile.sendByteBufPacket(0); */
			} else if (info instanceof INetworkReader) {
				OverlayBlockSelection.addPosition(((INetworkReader) info).getCoords(), false);
			}
			break;
		case STRING:
			if (info instanceof ComparableObject) {
				ComparableObject obj = (ComparableObject) info;
				((infoPos == 0) ? currentFilter.key1 : currentFilter.key2).setObject(obj.string);
			}
			break;
		default:
			break;

		}
	}

	@Override
	public void mouseClicked(int x, int y, int button) throws IOException {
		if (coolDown != 0) {
			return;
		}
		super.mouseClicked(x, y, button);
	}

	@Override
	public void setInfo() {
		switch (state) {
		case LIST:
			infoList = Lists.newArrayList(tile.getStatements().getObjects());
			break;
		case STATEMENT:
			break;
		case CHANNELS:
			infoList = Lists.newArrayList(ClientInfoHandler.instance().sortedLogicMonitors.getOrDefault(tile.getIdentity(), new ArrayList<>()));
			break;
		case STRING:
			InfoUUID uuid = (InfoUUID) (infoPos == 0 ? currentFilter.uuid1.getObject() : currentFilter.uuid2.getObject());
			if (InfoUUID.valid(uuid)) {
				IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(uuid);
				if (monitorInfo instanceof IComparableInfo) {
					IComparableInfo comparable = (IComparableInfo) monitorInfo;
					List objects = new ArrayList<ComparableObject>();
					comparable.getComparableObjects(objects);
					infoList = objects;
					break;
				}
			}
			infoList = new ArrayList<>();
			break;
		default:
			break;

		}
	}

	public void onTextFieldChanged(SonarTextField field) {
		super.onTextFieldChanged(field);
		if (field == inputField) {
			switch (currentFilter.getInputType()) {
			case INFO:
				break;
			case NUMBER:
				currentFilter.obj.set(field.getText().isEmpty() ? 0 : Double.valueOf(field.getText()), ObjectType.DOUBLE);
				break;
			case STRING:
				currentFilter.obj.set(field.getText(), ObjectType.STRING);
				break;
			default:

			}
		}

	}

	@Override
	public void keyTyped(char c, int i) throws IOException {
		if (this.getFocusedField() == null && state != GuiState.LIST && (i == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(i))) {
			if (state == GuiState.CHANNELS || state == GuiState.STRING) {
				changeState(GuiState.STATEMENT);
				return;
			}
			changeState(GuiState.LIST);
			return;
		} else {
			super.keyTyped(c, i);
		}
	}

}
