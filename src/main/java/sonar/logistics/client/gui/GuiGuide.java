package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.PL2Translate;
import sonar.logistics.client.LogisticsButton;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.generic.GuiSelectionList;
import sonar.logistics.common.containers.ContainerGuide;
import sonar.logistics.guide.GuidePageRegistry;
import sonar.logistics.guide.IGuidePage;
import sonar.logistics.guide.elements.Element3DRenderer;

public class GuiGuide extends GuiSelectionList<IGuidePage> {

	public static IGuidePage currentPage;
	public static int pagePos;
	public static int currentPos = -1;
	public int lastPos = -1;
	public int lastPagePos = -1;
	private SonarTextField searchField;
	public boolean updateSearchList;
	public int coolDown = 0;

	public GuiGuide(EntityPlayer player) {
		super(new ContainerGuide(player), (IWorldPosition) null);
		listHeight = 20;
		this.xSize = 182 + 66;

	}

	public double listScale() {
		return 1;
	}

	public void initGui() {
		coolDown = 25;
		enableListRendering = currentPage == null;
		super.initGui();
		if (scroller != null)
			scroller.renderScroller = enableListRendering;
		if (currentPage != null) {
			currentPage.initGui(this, pagePos);
			buttonList.add(new LogisticsButton(this, 0, guiLeft + 6, guiTop + 3, 512 - 24, 0, 16, 11, PL2Translate.BUTTON_BACK.t(), ""));
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
		updateSearchList();
	}

	public void onTextFieldChanged(SonarTextField field) {
		if (field == searchField) {
			updateSearchList = true;
		}
	}

	public void updateSearchList() {
		String search = searchField == null ? "" : searchField.getText();
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
			if (searchField != null)
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

	public void updatePage() {
		currentPage = infoList.get(currentPos);
		pagePos = 0;
		resetLastPos();
		reset();
		Element3DRenderer.reset();
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (currentPage != null) {
			buttonAction(button.id);
		}

	}

	public void buttonAction(int buttonID) {
		switch (buttonID) {
		case 0:
			if (lastPos != -1) {
				this.setCurrentPage(this.lastPos, this.lastPagePos);
				this.lastPos = -1;
				this.lastPagePos = -1;
				return;
			} else if (currentPage != null) {
				this.currentPage = null;
				reset();
			} else {
				Element3DRenderer.reset();
				this.mc.thePlayer.closeScreen();
			}
			break;
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
			Element3DRenderer.reset();
			break;
		case -4:
			if (pagePos + 1 < currentPage.getPageCount()) {
				pagePos++;
			} else {
				pagePos = 0;
			}
			reset();
			Element3DRenderer.reset();
			break;
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (currentPage != null) {
			currentPage.drawPage(this, mouseX, mouseY, pagePos);
		}
	}

	public void drawGuiContainerForegroundLayer(int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		if (currentPage != null) {
			FontHelper.textCentre(currentPage.getDisplayName(), xSize, 6, -1);
			currentPage.drawForegroundPage(this, x, y, pagePos);
			FontHelper.textCentre(pagePos + 1 + " / " + currentPage.getPageCount(), xSize, 140, -1);
			FontHelper.textCentre(currentPos + 1 + " / " + infoList.size(), xSize, 152, -1);
		} else {
			FontHelper.textCentre(PL2Translate.GUIDE_TITLE.t(), xSize, 6, -1);
		}
		if (coolDown != 0) {
			coolDown--;
		}
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		RenderHelper.restoreBlendState();

		super.drawGuiContainerForegroundLayer(x, y);

	}

	public void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		if (currentPage != null) {
			currentPage.drawBackgroundPage(this, var2, var3, pagePos);
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
		if (keyCode == Keyboard.KEY_LEFT) {
			if (this.pagePos == 0) {
				this.buttonAction(-1);
				this.pagePos = currentPage.getPageCount() - 1;
				this.reset();
			} else {
				this.buttonAction(-3);
			}
			return;
		} else if (keyCode == Keyboard.KEY_RIGHT) {
			if (this.pagePos == currentPage.getPageCount() - 1) {
				this.buttonAction(-2);
			}else{
				this.buttonAction(-4);
			}
			return;
		}

		Element3DRenderer.reset();
		super.keyTyped(typedChar, keyCode);
	}
}
