package sonar.logistics.core.items.guide;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import sonar.core.client.gui.SonarTextField;
import sonar.core.helpers.FontHelper;
import sonar.logistics.PL2Translate;
import sonar.logistics.base.gui.GuiSelectionList;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.base.gui.buttons.LogisticsButton;
import sonar.logistics.core.items.guide.pages.elements.Element3DRenderer;
import sonar.logistics.core.items.guide.pages.pages.IGuidePage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiGuide extends GuiSelectionList<IGuidePage> {

	public static IGuidePage currentPage;
	public static int currentPageId = -1;
	public static int currentPageSection = 0;
	public int lastPageId = -1;
	public int lastPageSection = -1;
	private SonarTextField searchField;
	public boolean updateSearchList;
	public int coolDown = 0;

	////BUTTON IDS
	public static final int RETURN = 0;
	public static final int PREV_PAGE = -1;
	public static final int NEXT_PAGE = -2;
	public static final int PREV_SECTION = -3;
	public static final int NEXT_SECTION = -4;

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
			currentPage.initGui(this, currentPageSection);
			buttonList.add(new LogisticsButton(this, RETURN, guiLeft + 6, guiTop + 3, 512 - 24, 0, 16, 11, PL2Translate.BUTTON_BACK.t(), ""));
			buttonList.add(new GuiButton(PREV_PAGE, guiLeft + 6, guiTop + ySize - 26, 20, 20, "<<"));
			buttonList.add(new GuiButton(NEXT_PAGE, guiLeft + xSize - 26, guiTop + ySize - 26, 20, 20, ">>"));
			buttonList.add(new GuiButton(PREV_SECTION, guiLeft + 26, guiTop + ySize - 26, 20, 20, "<"));
			buttonList.add(new GuiButton(NEXT_SECTION, guiLeft + xSize - 26 - 20, guiTop + ySize - 26, 20, 20, ">"));
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
			lastPageId = currentPage == null ? -1 : currentPage.pageID();
			lastPageSection = currentPage == null ? -1 : currentPageSection;
			if (searchField != null)
				searchField.setText("");
			infoList = GuidePageRegistry.pages;
			for (int i = 0; i < infoList.size(); i++) {
				IGuidePage listPage = infoList.get(i);
				if (listPage.pageID() == pageID) {
					currentPageId = i;
					currentPage = infoList.get(currentPageId);
					currentPageSection = newPos;
					reset();
					break;
				}
			}
		}
	}

	public void resetLastPos() {
		this.lastPageId = -1;
		this.lastPageSection = -1;
	}

	public void updatePage() {
		currentPage = infoList.get(currentPageId);
		currentPageSection = 0;
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
		case RETURN:
			if (lastPageId != -1) {
				this.setCurrentPage(this.lastPageId, this.lastPageSection);
				this.lastPageId = -1;
				this.lastPageSection = -1;
				return;
			} else if (currentPage != null) {
				currentPage = null;
				currentPageId = -1;
				currentPageSection = 0;
				reset();
			} else {
				Element3DRenderer.reset();
				this.mc.player.closeScreen();
			}
			break;
		case PREV_PAGE:
			if (currentPageId - 1 >= 0) {
				currentPageId--;
				updatePage();
			} else {
				currentPageId = infoList.size() - 1;
				updatePage();
			}
			break;
		case NEXT_PAGE:
			if (currentPageId + 1 < infoList.size()) {
				currentPageId++;
				updatePage();
			} else {
				currentPageId = 0;
				updatePage();
			}
			break;
		case PREV_SECTION:
			if (currentPageSection - 1 >= 0) {
				currentPageSection--;
			} else {
				currentPageSection = currentPage.getPageCount() - 1;
			}
			reset();
			Element3DRenderer.reset();
			break;
		case NEXT_SECTION:
			if (currentPageSection + 1 < currentPage.getPageCount()) {
				currentPageSection++;
			} else {
				currentPageSection = 0;
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
			currentPage.drawPage(this, mouseX, mouseY, currentPageSection);
			//net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		}
	}

	public void drawGuiContainerForegroundLayer(int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (currentPage != null) {
			FontHelper.textCentre(currentPage.getDisplayName(), xSize, 6, -1);
			currentPage.drawForegroundPage(this, x, y, currentPageSection, 0);
			FontHelper.textCentre("Sub Page: " + (currentPageSection + 1) + "/" + currentPage.getPageCount(), xSize, ySize - 26, -1);
			FontHelper.textCentre("Page: " + (currentPageId + 1) + "/" + infoList.size(), xSize, ySize - 16, -1);
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
			currentPage.drawBackgroundPage(this, var2, var3, currentPageSection);
		}
	}

	@Override
	public void renderInfo(IGuidePage info, int yPos) {
		info.drawPageInGui(this, yPos);
	}

	@Override
	public void selectionPressed(GuiButton button, int infoPos, int buttonID, IGuidePage info) {
		currentPage = info;
		currentPageSection = 0;
		currentPageId = infoPos;
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
		return PL2Colours.getDefaultSelection().getRGB();
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
		if (currentPage != null && keyCode == Keyboard.KEY_LEFT) {
			if (currentPageSection == 0) {
				this.buttonAction(PREV_PAGE);
				currentPageSection = currentPage.getPageCount() - 1;
				this.reset();
			} else {
				this.buttonAction(PREV_SECTION);
			}

			return;
		} else if (keyCode == Keyboard.KEY_RIGHT) {
			if (currentPage == null || currentPageSection == currentPage.getPageCount() - 1) {
				this.buttonAction(NEXT_PAGE);
			} else {
				this.buttonAction(NEXT_SECTION);
			}
			return;
		}

		Element3DRenderer.reset();
		if ((keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) && currentPage != null) {
			if (lastPageId == -1 || lastPageSection == -1) {
				currentPage = null;
				reset();
				return;
			}
			setCurrentPage(lastPageId, lastPageSection);
			resetLastPos();
			reset();
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}
}
