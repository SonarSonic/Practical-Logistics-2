package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import sonar.core.SonarCore;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.network.PacketFlexibleCloseGui;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.common.containers.ContainerGuide;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.guide.Guide3DRenderer;
import sonar.logistics.guide.GuidePageRegistry;
import sonar.logistics.guide.IGuidePage;

public class GuiGuide extends GuiSelectionList<IGuidePage> {

	public IGuidePage currentPage;
	public int pagePos;
	public int currentPos = -1;
	public int lastPos = -1;
	public int lastPagePos = -1;
	private SonarTextField searchField;
	public boolean updateSearchList;
	public int coolDown = 0;
	public List<GuiButton> guideButtons = new ArrayList();

	public GuiGuide(EntityPlayer player) {
		super(new ContainerGuide(player), (IWorldPosition) null);
		listHeight = 20;
		this.xSize = 182 + 66;

	}

	public double listScale() {
		return 1;
	}

	public void initGui() {
		guideButtons.clear();
		coolDown = 25;
		enableListRendering = currentPage == null;
		super.initGui();
		scroller.renderScroller = enableListRendering;
		if (currentPage != null) {
			currentPage.initGui(this, pagePos);
			buttonList.add(new GuiButton(-1, guiLeft + 6, guiTop + 140, 20, 20, "<<"));
			buttonList.add(new GuiButton(-2, guiLeft + 222, guiTop + 140, 20, 20, ">>"));
			buttonList.add(new GuiButton(-3, guiLeft + 26, guiTop + 140, 20, 20, "<"));
			buttonList.add(new GuiButton(-4, guiLeft + 202, guiTop + 140, 20, 20, ">"));
		} else {
			infoList = GuidePageRegistry.pages;
			Keyboard.enableRepeatEvents(true);
			searchField = new SonarTextField(0, this.fontRendererObj, 34, 17, 180, 10);
			searchField.setMaxStringLength(20);
			searchField.setText("");
			fieldList.add(searchField);
		}
	}

	public void onTextFieldChanged(SonarTextField field) {
		if (field == searchField) {
			updateSearchList = true;
		}
	}

	public void updateSearchList() {
		String search = searchField.getText();
		ArrayList<IGuidePage> searchList = new ArrayList();
		for (IGuidePage page : (ArrayList<IGuidePage>) GuidePageRegistry.pages.clone()) {
			if (page != null && page.getDisplayName().toLowerCase().contains(search.toLowerCase())) {
				searchList.add(page);
			}
		}
		infoList = searchList;
	}

	public void setCurrentPage(int pageID, int newPos) {
		if (currentPage == null || currentPage.pageID() != pageID) {
			lastPos = currentPage == null ? -1 : currentPage.pageID();
			lastPagePos = currentPage == null ? -1 : pagePos;
			searchField.setText("");
			infoList = (ArrayList<IGuidePage>) GuidePageRegistry.pages.clone();
			for (int i = 0; i < infoList.size(); i++) {
				IGuidePage listPage = infoList.get(i);
				if (listPage.pageID() == pageID) {
					currentPos = i;
					currentPage = infoList.get(currentPos);
					pagePos = newPos;
					reset();
					break;
				}
			}
		}
	}

	public void resetLastPos() {
		this.lastPos = -1;
		this.lastPagePos = -1;		
	}
	
	public void updatePage(){
		currentPage = infoList.get(currentPos);
		pagePos = 0;
		resetLastPos();
		reset();
		Guide3DRenderer.reset();
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (currentPage != null) {
			switch (button.id) {
			case -1:
				if (currentPos - 1 >= 0) {
					currentPos--;
					updatePage();
				} else {
					currentPos = infoList.size() - 1;
					updatePage();
				}
				break;
			case -2:
				if (currentPos + 1 < infoList.size()) {
					currentPos++;
					updatePage();
				} else {
					currentPos = 0;
					updatePage();
				}
				break;
			case -3:
				if (pagePos - 1 >= 0) {
					pagePos--;
				} else {
					pagePos = currentPage.getPageCount() - 1;
				}
				reset();
				Guide3DRenderer.reset();
				break;
			case -4:
				if (pagePos + 1 < currentPage.getPageCount()) {
					pagePos++;
				} else {
					pagePos = 0;
				}
				reset();
				Guide3DRenderer.reset();
				break;
			}
		}

	}

	public void drawGuiContainerForegroundLayer(int x, int y) {
		if (coolDown != 0) {
			coolDown--;
		}
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		RenderHelper.restoreBlendState();
		super.drawGuiContainerForegroundLayer(x, y);

		if (currentPage != null) {
			FontHelper.textCentre(currentPage.getDisplayName(), xSize, 6, LogisticsColours.white_text);
			currentPage.drawPage(this, x, y, pagePos);
			FontHelper.textCentre(pagePos + 1 + " / " + currentPage.getPageCount(), xSize, 140, LogisticsColours.white_text);
			FontHelper.textCentre(currentPos + 1 + " / " + infoList.size(), xSize, 152, LogisticsColours.white_text);

		} else {
			FontHelper.textCentre(FontHelper.translate("Practical Logistics Guide"), xSize, 6, LogisticsColours.white_text);
		}
	}

	@Override
	public void renderInfo(IGuidePage info, int yPos) {
		info.drawPageInGui(this, yPos);
	}

	@Override
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, IGuidePage info) {
		currentPage = info;
		pagePos = 0;
		currentPos = infoPos;
		reset();
	}

	public void mouseClicked(int x, int y, int button) throws IOException {
		super.mouseClicked(x, y, button);		
		if (currentPage != null && coolDown == 0)
			currentPage.mouseClicked(this, x, y, button);
	}

	@Override
	public void setInfo() {
		if (updateSearchList) {
			updateSearchList();
		}
	}

	@Override
	public int getColour(int i, int type) {
		return LogisticsColours.getDefaultSelection().getRGB();
	}

	@Override
	public boolean isPairedInfo(IGuidePage info) {
		return false;
	}

	@Override
	public boolean isSelectedInfo(IGuidePage info) {
		return false;
	}

	@Override
	public boolean isCategoryHeader(IGuidePage info) {
		return false;
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if ((keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))) {
			if (lastPos != -1) {
				this.setCurrentPage(this.lastPos, this.lastPagePos);
				this.lastPos = -1;
				this.lastPagePos = -1;
				return;
			} else if (currentPage != null) {
				this.currentPage = null;
				reset();
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
}
