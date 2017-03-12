package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;
import sonar.core.client.gui.SonarTextField;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.Logistics;
import sonar.logistics.api.displays.DisplayConstants;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.readers.INetworkReader;
import sonar.logistics.client.DisplayTextFields;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.common.multiparts.ScreenMultipart;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;
import sonar.logistics.helpers.InfoRenderer;

public class GuiDisplayScreen extends GuiSelectionList<Object> {
	public ScreenMultipart part;
	public DisplayTextFields textFields;
	private GuiState state = GuiState.LIST;
	private int left = 7;
	public int infoID = -1;
	public int coolDown = 0;

	public enum GuiState {
		LIST(176, 0), EDIT(176, 166), SOURCE(176 + 72, 166), CREATE(176, 166);
		int xSize, ySize;

		GuiState(int xSize, int ySize) {
			this.xSize = xSize;
			this.ySize = ySize;
		}
	}

	public enum ButtonType {
		EDIT("Edit"), SOURCE("Source");

		String text;

		ButtonType(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	public GuiDisplayScreen(ScreenMultipart part) {
		super(new ContainerMultipartSync(part), part);
		this.part = part;
		this.ySize = 20 + part.maxInfo() * 26;
		this.enableListRendering = false;
	}

	public void initGui() {
		super.initGui();
		switch (state) {
		case CREATE:
			break;
		case EDIT:
			this.buttonList.add(new GuiButton(0, guiLeft + 8, guiTop + 5, 40, 20, "Data"));
			this.buttonList.add(new GuiButton(1, guiLeft + 48, guiTop + 5, 40, 20, "Name"));
			this.buttonList.add(new GuiButton(2, guiLeft + 88, guiTop + 5, 40, 20, "Prefix"));
			this.buttonList.add(new GuiButton(3, guiLeft + 128, guiTop + 5, 40, 20, "Suffix"));
			this.buttonList.add(new GuiButton(4, guiLeft + 8, guiTop + 130 + 8, 50, 20, "RESET"));
			this.buttonList.add(new GuiButton(5, guiLeft + 8+50, guiTop + 130 + 8, 50, 20, "CLEAR"));
			this.buttonList.add(new GuiButton(6, guiLeft + 108, guiTop + 130 + 8, 50, 20, "SAVE"));
			ArrayList<String> strings = textFields == null ? part.container().getDisplayInfo(infoID).getUnformattedStrings() : textFields.textList();
			textFields = new DisplayTextFields(8, 28 + 4, 8);
			textFields.initFields(strings);
			break;
		case LIST:
			this.buttonList.add(new LogisticsButton(this, -1, guiLeft + 137, guiTop + 3, 64, 0 + 16 * part.getLayout().ordinal(), "Layout: " + part.getLayout(), "button.ScreenLayout"));
			// int width = 162;
			int height = 20;
			int left = 7;
			for (int i = 0; i < part.maxInfo(); i++) {
				int top = 22 + ((height + 6) * i);
				this.buttonList.add(new LogisticsButton(this, i, guiLeft + 130 - 3, guiTop + top, 32, 256 - 32, "Edit", ""));
				this.buttonList.add(new LogisticsButton(this, i + 100, guiLeft + 130 - 3 + 20, guiTop + top, 32, 256 - 16, "Source", ""));
			}

			break;
		case SOURCE:
			scroller = new SonarScroller(this.guiLeft + 164 + 71, this.guiTop + 29, 134, 10);
			for (int i = 0; i < size; i++) {
				this.buttonList.add(new SelectionButton(this, 10 + i, guiLeft + 7, guiTop + 29 + (i * 12), listHeight));
			}
			break;
		default:
			break;
		}
		// this.buttonList.add(new DisplayButton(3, ButtonType.EDIT, guiLeft + 130 - 3, guiTop + 20));
		/* nameField = new GuiTextField(0, this.fontRendererObj, 8, 18, 160, 12); nameField.setMaxStringLength(20); nameField.setText(part.getEmitterName()); */
	}


	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		if (coolDown != 0) {
			coolDown--;
		}
	}

	public void actionPerformed(GuiButton button) {
		if (coolDown != 0 || button == null) {
			return;
		}
		switch (state) {
		case CREATE:
			break;
		case EDIT:
			GuiTextField field = textFields.getSelectedField();
			switch (button.id) {
			case 0:
				if (field != null)
					field.writeText(DisplayConstants.DATA);
				break;
			case 1:
				if (field != null)
					field.writeText(DisplayConstants.NAME);
				break;
			case 2:
				if (field != null)
					field.writeText(DisplayConstants.PREFIX);
				break;
			case 3:
				if (field != null)
					field.writeText(DisplayConstants.SUFFIX);
				break;
			case 4:
				textFields.initFields(part.container().getDisplayInfo(infoID).getUnformattedStrings());
				break;
			case 5:
				textFields.initFields(new ArrayList());
				break;
			case 6:
				part.container().getDisplayInfo(infoID).setFormatStrings(textFields.textList());
				part.currentSelected = infoID;
				part.sendByteBufPacket(1);
				changeState(GuiState.LIST, -1);
				break;
			}
			break;
		case LIST:
			if (button.id == -1) {
				part.incrementLayout();
				reset();
				part.sendByteBufPacket(2);
				break;
			}
			if (button.id >= 100) {
				changeState(GuiState.SOURCE, button.id - 100);
			} else {
				changeState(GuiState.EDIT, button.id);
			}
			break;
		case SOURCE:
			super.actionPerformed(button);
			break;
		default:
			break;

		}
	}

	public void changeState(GuiState state, int btnID) {
		this.state = state;
		this.infoID = btnID;
		this.xSize = state.xSize;
		this.ySize = state == GuiState.LIST ? 20 + part.maxInfo() * 26 : state.ySize;
		this.enableListRendering = state == GuiState.SOURCE;
		if (scroller != null)
			this.scroller.renderScroller = state == GuiState.SOURCE;
		coolDown = state != GuiState.LIST ? 25 : 0;
		this.reset();
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		switch (state) {
		case CREATE:
			break;
		case EDIT:
			textFields.drawTextBox();
			break;
		case LIST:
			FontHelper.textCentre(FontHelper.translate("item.DisplayScreen.name"), xSize, 6, LogisticsColours.white_text.getRGB());
			RenderHelper.saveBlendState();
			for (int i = 0; i < part.maxInfo(); i++) {
				drawInfo(i, i < size ? part.container().getDisplayInfo(i) : null);
			}
			RenderHelper.restoreBlendState();
			break;
		case SOURCE:
			FontHelper.textCentre(FontHelper.translate("Info Selection"), xSize, 6, LogisticsColours.white_text);
			FontHelper.textCentre(String.format("Select the info you wish to display"), xSize, 18, LogisticsColours.grey_text);
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) throws IOException {
		if (coolDown != 0) {
			return;
		}
		if (state == GuiState.EDIT) {
			textFields.mouseClicked(i - guiLeft, j - guiTop, k);
		}
		super.mouseClicked(i, j, k);
		// nameField.mouseClicked(i - guiLeft, j - guiTop, k);
	}

	@Override
	public void keyTyped(char c, int i) throws IOException {
		if (state == GuiState.EDIT) {
			if (textFields.isFocused()) {
				textFields.keyTyped(c, i);
				return;
			}
		}
		if (state != GuiState.LIST && (i == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(i))) {
			changeState(GuiState.LIST, -1);
			return;
		}

		super.keyTyped(c, i);
		/* if (nameField.isFocused()) { if (c == 13 || c == 27) { nameField.setFocused(false); } else { nameField.textboxKeyTyped(c, i); final String text = nameField.getText(); setString((text.isEmpty() || text == "" || text == null) ? "Unnamed Emitter" : text); } } else { super.keyTyped(c, i); } */
	}

	public void setString(String string) {
		// this.part.emitterName.setObject(string);
		// part.sendByteBufPacket(2);
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
			FontHelper.text(!info.getUnformattedStrings().isEmpty() ? "CUSTOM DATA" : "NO DATA", 11, top + 6, LogisticsColours.white_text.getRGB());
		}
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		RenderHelper.restoreBlendState();
	}
	/* @SideOnly(Side.CLIENT) public class DisplayButton extends AnimatedButton { public int id; public ButtonType type; public DisplayButton(int id, ButtonType type, int x, int y) { super(id, x, y, GuiInventoryReader.sorting_icons, 15, 15); this.id = id; this.type = type; } public void drawButtonForegroundLayer(int x, int y) { drawCreativeTabHoveringText(type.getText(), x, y); }
	 * @Override public void onClicked() { }
	 * @Override public int getTextureX() { switch (type) { case EDIT: return 0; case SOURCE: return 16; default: break; } return 0; }
	 * @Override public int getTextureY() { return 16; } } */

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(Object info) {
		if (info instanceof IInfoProvider) {
			if (!RenderBlockSelection.positions.isEmpty()) {
				if (RenderBlockSelection.isPositionRenderered(((IInfoProvider) info).getCoords())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isCategoryHeader(Object info) {
		return info instanceof IInfoProvider;
	}

	@Override
	public boolean isSelectedInfo(Object info) {
		// if (part.getChannels(infoID).contains(info.syncCoords.getCoords())) {
		// return true;
		// }
		return info instanceof InfoUUID && part.container().getInfoUUID(infoID) != null && part.container().getInfoUUID(infoID).equals(info);
	}

	@Override
	public void renderInfo(Object info, int yPos) {
		if (info instanceof InfoUUID) {
			IMonitorInfo monitorInfo = Logistics.getClientManager().info.get((InfoUUID) info);
			if (monitorInfo != null) {
				InfoRenderer.renderMonitorInfoInGUI(monitorInfo, yPos + 1, LogisticsColours.white_text.getRGB());
			} else {

				FontHelper.text("-", InfoRenderer.identifierLeft, yPos, LogisticsColours.white_text.getRGB());
			}
		} else if (info instanceof IInfoProvider) {
			IInfoProvider monitor = (IInfoProvider) info;
			InfoRenderer.renderMonitorInfoInGUI(new MonitoredBlockCoords(monitor.getCoords(), monitor.getDisplayName()), yPos + 1, LogisticsColours.white_text.getRGB());
		}
		// ILogicMonitor monitor = LogicMonitorManager.getMonitorFromClient(info.uuid.getUUID().hashCode());
		// InfoRenderer.renderMonitorInfoInGUI(new MonitoredBlockCoords(info.coords.getCoords(), monitor.getClass().getSimpleName()), yPos + 1, LogisticsColours.white_text.getRGB());
	}

	@Override
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, Object info) {
		if (buttonID == 0 && info instanceof InfoUUID) {
			part.container().setUUID((InfoUUID) info, infoID);
			part.currentSelected = infoID;
			part.sendByteBufPacket(0);
		} else if (info instanceof IInfoProvider) {
			RenderBlockSelection.addPosition(((IInfoProvider) info).getCoords(), false);
		}
	}

	@Override
	public void setInfo() {
		infoList = (ArrayList<Object>) Logistics.getClientManager().sortedLogicMonitors.getOrDefault(part.getIdentity(), new ArrayList()).clone();
	}

}
