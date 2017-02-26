package sonar.logistics.client.gui;

import java.io.IOException;
import java.util.ArrayList;

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
import sonar.logistics.guide.GuidePageRegistry;
import sonar.logistics.guide.IGuidePage;

public class GuiGuide extends GuiSelectionList<IGuidePage> {

	public IGuidePage currentPage;
	public int pagePos;
	private SonarTextField searchField;
	public boolean updateSearchList;

	public GuiGuide(EntityPlayer player) {
		super(new ContainerGuide(player), (IWorldPosition) null);
		listHeight = 20;
		this.xSize = 182 + 66;

	}

	public double listScale() {
		return 1;
	}

	public void initGui() {
		enableListRendering = currentPage == null;
		super.initGui();
		scroller.renderScroller = enableListRendering;
		if (currentPage != null) {
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
			ItemStack item = page.getItemStack();
			if (item != null && item != null && item.getDisplayName().toLowerCase().contains(search.toLowerCase())) {
				searchList.add(page);
			}
		}
		infoList = searchList;
	}

	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (currentPage != null) {
			switch (button.id) {
			case -1:

				break;
			case -2:

				break;
			case -3:
				if (pagePos - 1 >= 0)
					pagePos--;
				break;
			case -4:
				if (pagePos + 1 < currentPage.getPageCount())
					pagePos++;
				break;
			}
		}

	}

	public void drawGuiContainerForegroundLayer(int x, int y) {
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		RenderHelper.restoreBlendState();
		super.drawGuiContainerForegroundLayer(x, y);

		if (currentPage != null) {
			FontHelper.textCentre(currentPage.getItemStack().getDisplayName(), xSize, 6, LogisticsColours.white_text);
			currentPage.drawPage(this, x, y, pagePos);
			FontHelper.textCentre(pagePos + 1 + " / " + currentPage.getPageCount(), xSize, 146, LogisticsColours.white_text);

		} else {
			FontHelper.textCentre(FontHelper.translate("Practical Logistics Guide"), xSize, 6, LogisticsColours.white_text);
		}
	}

	@Override
	public void renderInfo(IGuidePage info, int yPos) {
		RenderHelper.saveBlendState();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		ItemStack stack = info.getItemStack();
		RenderHelper.renderItem(this, 8, yPos - 1, stack);
		RenderHelper.renderStoredItemStackOverlay(stack, 0, 8, yPos - 1, null, true);
		RenderHelper.restoreBlendState();
		FontHelper.text(stack.getDisplayName(), 28, yPos + 3, LogisticsColours.white_text);

	}

	@Override
	public void selectionPressed(GuiButton button, int buttonID, IGuidePage info) {
		currentPage = info;
		pagePos = 0;
		reset();
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
		if ((keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) && currentPage != null) {
			this.currentPage = null;
			reset();
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}
}
