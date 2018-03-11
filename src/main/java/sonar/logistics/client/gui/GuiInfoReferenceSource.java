package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.client.FMLClientHandler;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.ListHelper;
import sonar.logistics.PL2;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IInfoReferenceRequirement;
import sonar.logistics.api.displays.elements.IInfoRequirement;
import sonar.logistics.api.displays.references.InfoReference;
import sonar.logistics.api.displays.references.ReferenceType;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.client.gui.generic.info.InfoReferenceRequest;
import sonar.logistics.client.gui.generic.info.InfoUUIDRequest;
import sonar.logistics.helpers.InfoRenderer;

public class GuiInfoReferenceSource extends GuiSelectionList<Object> {

	public IInfoReferenceRequirement element;
	public List<InfoReference> selected;
	public List<InfoUUID> expanded = Lists.newArrayList();
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
		if (info instanceof InfoUUID) {
			InfoUUID uuid = (InfoUUID) info;
			boolean isSelected = false;
			for (InfoReference ref : selected) {
				if (ref.uuid.equals(uuid)) {
					isSelected = true;
					break;
				}
			}
			return (!isSelected || expanded.contains(uuid)) ? false : true;
		}
		return info instanceof InfoReference && selected.contains(info);
	}

	@Override
	public boolean isCategoryHeader(Object info) {
		return info instanceof IInfoProvider;
	}

	private IInfo last;
	
	@Override
	public void renderInfo(Object info, int yPos) {
		if (info instanceof InfoUUID) {
			last = PL2.getClientManager().info.get((InfoUUID) info);
			if (last != null) {
				InfoRenderer.renderMonitorInfoInGUI(last, yPos + 1, LogisticsColours.white_text.getRGB());
			} else {
				FontHelper.text("-", InfoRenderer.left_offset, yPos, LogisticsColours.white_text.getRGB());
			}
		} else if (info instanceof IInfoProvider) {
			IInfoProvider monitor = (IInfoProvider) info;
			FontHelper.text(monitor.getMultipart().getDisplayName(), InfoRenderer.left_offset, yPos, LogisticsColours.white_text.getRGB());
			FontHelper.text(monitor.getCoords().toString(), InfoRenderer.middle_offset, yPos, LogisticsColours.white_text.getRGB());
			FontHelper.text("position", InfoRenderer.right_offset, yPos, LogisticsColours.white_text.getRGB());
		} else if (info instanceof InfoReference){
			InfoReference ref = (InfoReference) info;
			InfoRenderer.renderTripleStringIntoGUI("  -" + ref.refType.name(), ref.refType.getRefString(last), "string",  yPos, LogisticsColours.white_text.getRGB());
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
			RenderBlockSelection.addPosition(((IInfoProvider) info).getCoords(), false);
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
		List<Object> newInfo = Lists.newArrayList(PL2.getClientManager().sortedLogicMonitors.getOrDefault(gsi.getDisplayGSIIdentity(), Lists.newArrayList()));
		if (!expanded.isEmpty()) {
			ListIterator it = newInfo.listIterator();
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof InfoUUID && expanded.contains(next)) {
					IInfo monitorInfo = PL2.getClientManager().info.get((InfoUUID) next);
					for (ReferenceType type : ReferenceType.values()) {
						it.add(new InfoReference((InfoUUID) next, type, monitorInfo.getID().hashCode()));
					}
				}
			}
		}
		infoList = newInfo;

	}

}
