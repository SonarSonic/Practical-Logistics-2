package sonar.logistics.base.requests.reference;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.client.FMLClientHandler;
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
import sonar.logistics.core.tiles.displays.info.references.InfoReference;
import sonar.logistics.core.tiles.displays.info.references.ReferenceType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class GuiInfoReferenceSource extends GuiSelectionList<Object> {

	public IInfoReferenceRequirement element;
	public List<InfoReference> selected;
	public List<InfoUUID> expanded = new ArrayList<>();
	public DisplayGSI gsi;

	public GuiInfoReferenceSource(IInfoReferenceRequirement element, DisplayGSI gsi, Container container) {
		super(container, gsi.getDisplay());
		this.element = element;
		this.gsi = gsi;
		this.selected = element.getSelectedReferences();
		this.xSize = 176 + 72;
		this.ySize = 166;
	}

	public void initGui() {
		super.initGui();
		this.xSize = 176 + 72;
		this.ySize = 166;
		//scroller = new SonarScroller(this.guiLeft + 164 + 71, this.guiTop + 29, 134, 10);
		//for (int i = 0; i < size; i++) {
		//	this.buttonList.add(new SelectionButton(this, 10 + i, guiLeft + 7, guiTop + 29 + (i * 12), listWidth, listHeight));
		//}
	}
	
	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		FontHelper.textCentre("Info Source", xSize, 6, PL2Colours.white_text);
		FontHelper.textCentre("Select data to display", xSize, 18, PL2Colours.grey_text);
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
		if (info instanceof InfoUUID) {
			InfoUUID uuid = (InfoUUID) info;
			boolean isSelected = false;
			for (InfoReference ref : selected) {
				if (ref.uuid.equals(uuid)) {
					isSelected = true;
					break;
				}
			}
			return isSelected && !expanded.contains(uuid);
		}
		return info instanceof InfoReference && selected.contains(info);
	}

	@Override
	public boolean isCategoryHeader(Object info) {
		return info instanceof ClientLocalProvider;
	}

	private IInfo last;
	
	@Override
	public void renderInfo(Object info, int yPos) {
		if (info instanceof InfoUUID) {
			last = ClientInfoHandler.instance().getInfoMap().get(info);
			if (last != null) {
				InfoRenderHelper.renderMonitorInfoInGUI(last, yPos + 1, PL2Colours.white_text.getRGB());
			} else {
				FontHelper.text("-", InfoRenderHelper.left_offset, yPos, PL2Colours.white_text.getRGB());
			}
		}  else if (info instanceof ClientLocalProvider) {
			ClientLocalProvider monitor = (ClientLocalProvider) info;
			FontHelper.text(monitor.stack.getDisplayName(), InfoRenderHelper.left_offset, yPos, PL2Colours.white_text.getRGB());
			FontHelper.text(monitor.coords.getCoords().toString(), InfoRenderHelper.middle_offset, yPos, PL2Colours.white_text.getRGB());
		} else if (info instanceof InfoReference){
			InfoReference ref = (InfoReference) info;
			InfoRenderHelper.renderTripleStringIntoGUI("  -" + ref.refType.name(), ref.refType.getRefString(last), "string",  yPos, PL2Colours.white_text.getRGB());
		}
	}

	@Override
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, Object info) {
		if (buttonID == 0 && info instanceof InfoReference) {
			InfoReference uuid = (InfoReference) info;
			if (selected.size() < element.getReferencesRequired()) {
				ListHelper.addWithCheck(selected, uuid);
			} else if (!selected.contains(uuid)) {
				selected.remove(0);
				selected.add(uuid);
			}
		} else if (info instanceof InfoUUID) {
			if (expanded.contains(info)) {
				expanded.remove(info);
			} else {
				expanded.add((InfoUUID) info);
			}
		} else if (info instanceof IInfoProvider) {
			OverlayBlockSelection.addPosition(((IInfoProvider) info).getCoords(), false);
		}
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		if (isCloseKey(i) && selected.size() == element.getReferencesRequired()) {
			element.onGuiClosed(selected);
		}
		if (element instanceof InfoReferenceRequest) {
			FMLClientHandler.instance().showGuiScreen(((InfoReferenceRequest) element).screen);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void setInfo() {
		List<Object> newInfo = Lists.newArrayList(ClientInfoHandler.instance().sortedLogicMonitors.getOrDefault(gsi.getDisplayGSIIdentity(), new ArrayList<>()));
		if (!expanded.isEmpty()) {
			ListIterator it = newInfo.listIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof InfoUUID && expanded.contains(next)) {
					IInfo monitorInfo = ClientInfoHandler.instance().getInfoMap().get(next);
					for (ReferenceType type : ReferenceType.values()) {
						it.add(new InfoReference((InfoUUID) next, type, monitorInfo.getID().hashCode()));
					}
				}
			}
		}
		infoList = newInfo;

	}

}
