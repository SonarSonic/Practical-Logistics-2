package sonar.logistics.client.gui.display;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import sonar.core.client.gui.IGridGui;
import sonar.core.client.gui.SelectionGrid;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.helpers.FontHelper;
import sonar.core.utils.CustomColour;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

public class GuiEditElementsList extends GuiAbstractEditGSI implements IGridGui {

	public Map<SelectionGrid, SonarScroller> grids = new HashMap<>();
	public List elements;
	public List<IDisplayElement> selected = new ArrayList<>();

	public GuiEditElementsList(DisplayGSI gsi, TileAbstractDisplay display) {
		super(gsi, display);
		updateElementsList();
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		Map<SelectionGrid, SonarScroller> newgrids = new HashMap<>();
		addGrids(newgrids);
		grids = newgrids;
		this.buttonList.add(new LogisticsButton(this, 0, guiLeft + 4 + 176 + 54, guiTop + 152 + 18 * 2, 32, 2 * 16, PL2Translate.BUTTON_DELETE.t(), ""));
		this.buttonList.add(new LogisticsButton(this, 1, guiLeft + 4 + 176 + 54, guiTop + 152, 12 * 16, 0, PL2Translate.BUTTON_EDIT.t(), ""));
		this.buttonList.add(new LogisticsButton(this, 2, guiLeft + 4 + 176 + 54, guiTop + 152 + 18 * 1, 12 * 16, 16, "Resize Element", ""));
		this.buttonList.add(new LogisticsButton(this, 3, guiLeft + 4 + 176 + 54, guiTop + 152 + 18 * 3, 32, 2 * 16, PL2Translate.BUTTON_RESET.t(), ""));

	}

	public void updateElementsList() {
		List elements = new ArrayList<>();
		for (DisplayElementContainer c : gsi.containers.values()) {
			if (!gsi.isEditContainer(c)) {
				elements.add(c);
				for (IDisplayElement e : c.getElements()) {
					elements.add(e);
				}
			}
		}
		this.elements = elements;
	}

	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		switch (button.id) {
		case 0:
			List<Integer> toDelete = getSelectedElementIdentities();
			if (!toDelete.isEmpty()) {
				GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createDeleteElementsPacket(toDelete), -1, gsi);
				List<IDisplayElement> elements = Lists.newArrayList(this.elements);
				selected.forEach(e -> elements.remove(e));
				selected.clear();
				this.elements = elements;
			}
			return;
		case 1:
			if (!selected.isEmpty()) {
				Optional<IDisplayElement> element = selected.stream().filter(e -> e != null).findFirst();
				if (element.isPresent()) {
					Object editScreen = element.get().getClientEditGui(display, this, mc.world, mc.player);
					if (editScreen != null) { // FIXME make a default if there is not edit gui
						FMLClientHandler.instance().showGuiScreen(editScreen);
					}
				}
			}
			break;
		case 2:
			if (!selected.isEmpty()) {
				Optional<IDisplayElement> element = selected.stream().filter(e -> e != null).findFirst();
				if (element.isPresent()) {
					gsi.grid_mode.startResizeSelectionMode(element.get().getHolder().getContainer().getContainerIdentity());
					this.mc.player.closeScreen();
				}
			}
			break;
		case 3:
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createResetGSIPacket(), -1, gsi);
			break;
		}
	}

	public List<Integer> getSelectedElementIdentities() {
		List<Integer> identities = new ArrayList<>();
		selected.forEach(e -> identities.add(e.getElementIdentity()));
		return identities;
	}

	public List<IDisplayElement> getGridList(int gridID) {
		if (gridID == 0) {
			return elements;
		}
		return null;
	}

	public void renderStrings(int x, int y) {}

	@Override
	public float getCurrentScroll(SelectionGrid gridID) {
		return grids.get(gridID).getCurrentScroll();
	}

	@Override
	public void onDisplayElementClicked(IDisplayElement e, DisplayScreenClick fakeClick, double[] subClick) {
		if (e != null) {
			if (selected.contains(e)) {
				selected.removeIf(ee -> e == ee);
			} else {
				selected.add((IDisplayElement) e);
			}
		}
	}

	@Override
	public void onGridClicked(int gridID, Object element, int x, int y, int pos, int button, boolean empty) {
		if (element instanceof IDisplayElement) {
			if (selected.contains(element)) {
				selected.remove(element);
			} else {
				selected.add((IDisplayElement) element);
			}
		}
	}

	@Override
	public void renderGridElement(int gridID, Object obj, int x, int y, int slot) {
		if (obj instanceof IDisplayElement) {
			IDisplayElement element = (IDisplayElement) obj;
			if (selected.contains(element)) {
				GlStateManager.disableLighting();
				drawTransparentRect(0, 0, defgridElementWidth - 2, defgridElementHeight, LogisticsColours.getDefaultSelection().getRGB());
			}
			FontHelper.text(element.getRepresentiveString(), 4, 3, -1);
			FontHelper.text("", 92, 3, -1);
			FontHelper.text(element.getRegisteredName(), 144, 3, -1);
		} else if (obj instanceof DisplayElementContainer) {
			DisplayElementContainer element = (DisplayElementContainer) obj;
			GlStateManager.disableLighting();
			drawTransparentRect(0, 0, defgridElementWidth - 2, defgridElementHeight, LogisticsColours.category.getRGB());
			FontHelper.text("Element Container: " + element.containerIdentity, 4, 3, -1);
		}
	}

	@Override
	public void renderElementToolTip(int gridID, Object element, int x, int y) {

	}

	//// from GuiSelectionGrid2 \\\\

	private int defgridXStart = 4, defgridYStart = 154, defgridElementWidth = 176 + 72 - 30, defgridElementHeight = 14, defgridWidth = 1, defgridHeight = 5;

	public void addGrids(Map<SelectionGrid, SonarScroller> grids) {
		SonarScroller grid_scroller = new SonarScroller(defgridXStart + (defgridElementWidth * defgridWidth), defgridYStart - 1, (defgridElementHeight * defgridHeight) + 2, 10);
		SelectionGrid grid = new SelectionGrid(this, 0, defgridXStart, defgridYStart, defgridElementWidth, defgridElementHeight, defgridWidth, defgridHeight);
		grids.put(grid, grid_scroller);
	}

	@Override
	public void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);
		if (button == 0 || button == 1) {
			grids.forEach((grid, scroll) -> grid.mouseClicked(this, x, y, button));
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int x, int y) {
		super.drawGuiContainerForegroundLayer(x, y);
		updateElementsList();
		GlStateManager.color(1f, 1f, 1f, 1f);
		renderStrings(x, y);
		GlStateManager.enableLighting();
		for (Entry<SelectionGrid, SonarScroller> entry : grids.entrySet()) {
			renderScroller(entry.getValue());
			entry.getKey().renderGrid(this, x, y);
		}
		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		DisplayElementHelper.align(getAlignmentTranslation());
		GlStateManager.translate(-guiLeft, -guiTop, 0);
		GlStateManager.scale(getActualScaling()[2], getActualScaling()[2], 1);
		for (DisplayElementContainer container : gsi.containers.values()) {
			boolean match = selected.stream().anyMatch(e -> e.getHolder().getContainer() == container);
			if (match && container.canRender() && !gsi.isEditContainer(container)) {
				double[] translation = container.getTranslation();
				double[] scaling = container.getContainerMaxScaling();
				sonar.core.helpers.RenderHelper.saveBlendState();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
				GlStateManager.disableDepth();
				DisplayElementHelper.drawRect(translation[0], translation[1], translation[0] + scaling[0], translation[1] + scaling[1], new CustomColour(49, 145, 88).getRGB());
				GlStateManager.disableBlend();
				sonar.core.helpers.RenderHelper.restoreBlendState();
			}
		}
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	public void startToolTipRender(int gridID, Object selection, int x, int y) {
		GlStateManager.disableDepth();
		GlStateManager.disableLighting();
		renderElementToolTip(gridID, selection, x, y);
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
	}

	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		grids.forEach((grid, scroll) -> scroll.handleMouse(grid));
	}

	public void drawScreen(int x, int y, float var) {
		super.drawScreen(x, y, var);
		grids.forEach((grid, scroll) -> {
			grid.setList(Lists.newArrayList(getGridList(grid.gridID)));
			scroll.drawScreen(x - guiLeft, y - guiTop, grid.isScrollable());
		});
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		GlStateManager.disableLighting();
		grids.forEach((grid, scroll) -> {
			drawTransparentRect(guiLeft + grid.xPos - 1, guiTop + grid.yPos - 1, guiLeft + grid.xPos + (grid.eWidth * grid.gWidth) - 1, guiTop + grid.yPos + (grid.eHeight * grid.gHeight) - 1, LogisticsColours.grey_base.getRGB());
			drawTransparentRect(guiLeft + grid.xPos, guiTop + grid.yPos, guiLeft + grid.xPos + (grid.eWidth * grid.gWidth) - 2, guiTop + grid.yPos + (grid.eHeight * grid.gHeight) - 2, LogisticsColours.blue_overlay.getRGB());
		});
	}

}
