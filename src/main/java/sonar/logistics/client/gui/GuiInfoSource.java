package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.ListHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IInfoRequirement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.client.gui.generic.info.InfoUUIDRequest;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.networking.ClientInfoHandler;

public class GuiInfoSource extends GuiSelectionList<Object> {

	public IInfoRequirement element;
	public List<InfoUUID> selected;
	public DisplayGSI gsi;

	public GuiInfoSource(IInfoRequirement element, DisplayGSI gsi, Container container) {
		super(container, gsi.getDisplay());
		this.element = element;
		this.gsi = gsi;
		this.selected = element.getSelectedInfo();
		this.xSize = 176 + 72;
		this.ySize = 166;
	}

	public void initGui() {
		super.initGui();
		this.xSize = 176 + 72;
		this.ySize = 166;
		scroller = new SonarScroller(this.guiLeft + 164 + 71, this.guiTop + 29, 134, 10);
		for (int i = 0; i < size; i++) {
			this.buttonList.add(new SelectionButton(this, 10 + i, guiLeft + 7, guiTop + 29 + (i * 12), listWidth, listHeight));
		}
	}
	
	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre(TextFormatting.BOLD + "Info Source", xSize, 6, LogisticsColours.white_text);
		FontHelper.textCentre("Select data to display", xSize, 18, LogisticsColours.white_text);
	}

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
	public boolean isSelectedInfo(Object info) {
		return info instanceof InfoUUID && selected.contains(info);
	}

	@Override
	public boolean isCategoryHeader(Object info) {
		return info instanceof IInfoProvider;
	}

	@Override
	public void renderInfo(Object info, int yPos) {
		if (info instanceof InfoUUID) {
			IInfo monitorInfo = ClientInfoHandler.instance().info.get((InfoUUID) info);
			if (monitorInfo != null) {
				InfoRenderer.renderMonitorInfoInGUI(monitorInfo, yPos + 1, LogisticsColours.white_text.getRGB());
			} else {
				FontHelper.text("-", InfoRenderer.left_offset, yPos, LogisticsColours.white_text.getRGB());
			}
		} else if (info instanceof IInfoProvider) {
			IInfoProvider monitor = (IInfoProvider) info;
			FontHelper.text(monitor.getMultipart().getDisplayName(), InfoRenderer.left_offset, yPos, LogisticsColours.white_text.getRGB());
			FontHelper.text(monitor.getCoords().toString(), InfoRenderer.middle_offset, yPos, LogisticsColours.white_text.getRGB());
			FontHelper.text("position", InfoRenderer.right_offset, yPos, LogisticsColours.white_text.getRGB());
		}
	}

	@Override
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, Object info) {
		if (buttonID == 0 && info instanceof InfoUUID) {
			InfoUUID uuid = (InfoUUID) info;
			if (selected.size() < element.getRequired()) {
				ListHelper.addWithCheck(selected, uuid);
			} else if (!selected.contains(uuid)) {
				selected.remove(0);
				selected.add(uuid);
			}
		} else if (info instanceof IInfoProvider) {
			RenderBlockSelection.addPosition(((IInfoProvider) info).getCoords(), false);
		}
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (isCloseKey(i) && selected.size() == element.getRequired()) {
			element.onGuiClosed(selected);
		}
		if(element instanceof InfoUUIDRequest){
			FMLClientHandler.instance().showGuiScreen(((InfoUUIDRequest) element).screen);			
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void setInfo() {
		infoList = Lists.newArrayList(ClientInfoHandler.instance().sortedLogicMonitors.getOrDefault(gsi.getDisplayGSIIdentity(), new ArrayList<>()));
	}

}
