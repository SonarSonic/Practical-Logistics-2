package sonar.logistics.base.requests.info;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.ListHelper;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.readers.ClientLocalProvider;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.gui.GuiSelectionList;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.base.gui.overlays.OverlayBlockSelection;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.info.InfoRenderHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		FontHelper.textCentre(TextFormatting.BOLD + "Info Source", xSize, 6, PL2Colours.white_text);
		FontHelper.textCentre("Select data to display", xSize, 18, PL2Colours.white_text);
	}

	@Override
	public int getColour(int i, int type) {
		return PL2Colours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(Object info) {
		if (info instanceof IInfoProvider) {
			if (!OverlayBlockSelection.positions.isEmpty()) {
                return OverlayBlockSelection.isPositionRenderered(((IInfoProvider) info).getCoords());
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
		return info instanceof ClientLocalProvider;
	}

	@Override
	public void renderInfo(Object info, int yPos) {
		if (info instanceof InfoUUID) {
			IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(info);
			if (monitorInfo != null) {
				InfoRenderHelper.renderMonitorInfoInGUI(monitorInfo, yPos + 1, PL2Colours.white_text.getRGB());
			} else {
				FontHelper.text("-", InfoRenderHelper.left_offset, yPos, PL2Colours.white_text.getRGB());
			}
		} else if (info instanceof ClientLocalProvider) {
			ClientLocalProvider monitor = (ClientLocalProvider) info;
			FontHelper.text(monitor.stack.getDisplayName(), InfoRenderHelper.left_offset, yPos, PL2Colours.white_text.getRGB());
			FontHelper.text(monitor.coords.getCoords().toString(), InfoRenderHelper.middle_offset, yPos, PL2Colours.white_text.getRGB());
			//FontHelper.text("position", InfoRenderHelper.right_offset, yPos, PL2Colours.white_text.getRGB());
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
			OverlayBlockSelection.addPosition(((IInfoProvider) info).getCoords(), false);
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
