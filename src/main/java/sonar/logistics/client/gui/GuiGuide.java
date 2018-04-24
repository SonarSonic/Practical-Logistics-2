package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
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
		super(new ContainerGuide(player), null);
		listHeight = 20;
		listWidth = 330;
		this.xSize = 350;// (182 + 66);
		this.ySize = 250;// 166

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
			buttonList.add(new GuiButton(-1, guiLeft + 6, guiTop + ySize - 26, 20, 20, "<<"));
			buttonList.add(new GuiButton(-2, guiLeft + xSize - 26, guiTop + ySize - 26, 20, 20, ">>"));
			buttonList.add(new GuiButton(-3, guiLeft + 26, guiTop + ySize - 26, 20, 20, "<"));
			buttonList.add(new GuiButton(-4, guiLeft + xSize - 26 - 20, guiTop + ySize - 26, 20, 20, ">"));
		} else {
			infoList = GuidePageRegistry.pages;
			Keyboard.enableRepeatEvents(true);
			searchField = new SonarTextField(0, this.fontRenderer, 50, 17, 240, 10);
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
		List<IGuidePage> searchList = new ArrayList<>();
		for (IGuidePage page : GuidePageRegistry.pages) {
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
			infoList = GuidePageRegistry.pages;
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
				currentPage = null;
				reset();
			} else {
				Element3DRenderer.reset();
				this.mc.player.closeScreen();
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
			//net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			currentPage.drawPage(this, mouseX, mouseY, pagePos);
			//net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		}
	}

	public void drawGuiContainerForegroundLayer(int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (currentPage != null) {
			FontHelper.textCentre(currentPage.getDisplayName(), xSize, 6, -1);
			currentPage.drawForegroundPage(this, x, y, pagePos, 0);
			FontHelper.textCentre("Sub Page: " + (pagePos + 1) + "/" + currentPage.getPageCount(), xSize, ySize - 26, -1);
			FontHelper.textCentre("Page: " + (currentPos + 1) + "/" + infoList.size(), xSize, ySize - 16, -1);
		} else {
			FontHelper.text("Search: ", 8, 18, -1);
			FontHelper.textCentre(PL2Translate.GUIDE_TITLE.t(), xSize, 6, -1);
		}
		if (coolDown != 0) {
			coolDown--;
		}
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
			if (pagePos == 0) {
				this.buttonAction(-1);
				pagePos = currentPage.getPageCount() - 1;
				this.reset();
			} else {
				this.buttonAction(-3);
			}
			return;
		} else if (keyCode == Keyboard.KEY_RIGHT) {
			if (pagePos == currentPage.getPageCount() - 1) {
				this.buttonAction(-2);
			} else {
				this.buttonAction(-4);
			}
			return;
		}
		Element3DRenderer.reset();
		if ((keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) && currentPage != null) {
			if (lastPos == -1 || lastPagePos == -1) {
				currentPage = null;
				reset();
				return;
			}
			setCurrentPage(lastPos, lastPagePos);
			resetLastPos();
			reset();
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}
}
