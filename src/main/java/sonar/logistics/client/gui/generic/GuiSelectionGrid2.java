package sonar.logistics.client.gui.generic;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import sonar.core.client.gui.IGridGui;
import sonar.core.client.gui.SelectionGrid;
import sonar.core.client.gui.widgets.SonarScroller;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiLogistics;

public abstract class GuiSelectionGrid2<T extends IInfo> extends GuiLogistics implements IGridGui {

	public Map<SelectionGrid, SonarScroller> grids = new HashMap<>();

	public GuiSelectionGrid2(Container container, IWorldPosition entity) {
		super(container, entity);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.mc.player.openContainer = this.inventorySlots;
		this.xSize = 176 + 72;
		this.ySize = 256;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		addGrids(grids);
	}

	private int defgridXStart = 13, defgridYStart = 32, defgridElementWidth = 18, defgridElementHeight = 18, defgridWidth = 12, defgridHeight = 7;
	
	public void addGrids(Map<SelectionGrid, SonarScroller> grids) {
		SonarScroller grid_scroller = new SonarScroller(this.guiLeft + defgridXStart + (defgridElementWidth * defgridWidth) + 3, this.guiTop + defgridYStart - 1, (defgridElementHeight * defgridHeight) + 2, 10);
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
		renderStrings(x, y);
		grids.forEach((grid, scroll) -> {
			renderScroller(scroll);
			grid.renderGrid(this, x, y);
		});
	}

	public void startToolTipRender(int gridID, T selection, int x, int y) {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GlStateManager.disableLighting();
		renderElementToolTip(gridID, selection, x, y);
		GlStateManager.enableLighting();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
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
			scroll.drawScreen(x, y, grid.isScrollable());
		});
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		this.renderPlayerInventory(40, 173);
		grids.forEach((grid, scroll) -> {
			drawTransparentRect(guiLeft + grid.xPos - 1, guiTop + grid.yPos - 1, guiLeft + grid.xPos + (grid.eWidth * grid.gWidth) - 1, guiTop + grid.yPos + (grid.eHeight * grid.gHeight) - 1, LogisticsColours.grey_base.getRGB());
			drawTransparentRect(guiLeft + grid.xPos, guiTop + grid.yPos, guiLeft + grid.xPos + (grid.eWidth * grid.gWidth) - 2, guiTop + grid.yPos + (grid.eHeight * grid.gHeight) - 2, LogisticsColours.blue_overlay.getRGB());
		});
		drawTransparentRect(guiLeft + 12, guiTop + 170, guiLeft + xSize - 9, guiTop + 252, LogisticsColours.grey_base.getRGB());
		drawTransparentRect(guiLeft + 13, guiTop + 171, guiLeft + xSize - 10, guiTop + 251, LogisticsColours.blue_overlay.getRGB());
	}

	public abstract List<T> getGridList(int gridID);

	public abstract void renderStrings(int x, int y);


}
