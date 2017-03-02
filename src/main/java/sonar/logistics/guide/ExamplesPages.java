package sonar.logistics.guide;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import sonar.core.helpers.FontHelper;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.client.gui.LogisticsButton;
import sonar.logistics.guide.ExampleConfigurations.FurnaceProgress;
import sonar.logistics.guide.ExampleConfigurations.InventoryExample;

public class ExamplesPages extends BaseInfoPage {

	public ExamplesPages(int pageID) {
		super(pageID);
	}

	public void initGui(GuiGuide gui, int subPage) {
		super.initGui(gui, subPage);
		int centreX = 13;
		int centreY = 24;
		guideButtons.add(new LogisticsButton(gui, 0, centreX, centreY, 512 - 8, 0, 8, 8, "", ""));
		guideButtons.add(new LogisticsButton(gui, 1, centreX, centreY - 8, 512 - 8, 8, 8, 8, "", ""));
		guideButtons.add(new LogisticsButton(gui, 2, centreX - 8, centreY, 512 - 8, 32, 8, 8, "", ""));
		guideButtons.add(new LogisticsButton(gui, 3, centreX + 8, centreY, 512 - 8, 24, 8, 8, "", ""));
		guideButtons.add(new LogisticsButton(gui, 4, centreX, centreY + 8, 512 - 8, 16, 8, 8, "", ""));
		guideButtons.add(new LogisticsButton(gui, 5, centreX - 8, centreY - 8, 512 - 8, 40, 8, 8, "", ""));
	}

	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			Guide3DRenderer.canRotate = !Guide3DRenderer.canRotate;
			break;
		case 1:
			Guide3DRenderer.rotateY += 10;
			if (Guide3DRenderer.rotateY >= 360) {
				Guide3DRenderer.rotateY = 0;
			}
			break;
		case 2:
			Guide3DRenderer.rotate -= 10;
			if (Guide3DRenderer.rotate <= 1) {
				Guide3DRenderer.rotate = 360;
			}
			break;
		case 3:
			Guide3DRenderer.rotate += 10;
			if (Guide3DRenderer.rotate >= 360) {
				Guide3DRenderer.rotate = 0;
			}
			break;
		case 4:
			Guide3DRenderer.rotateY -= 10;
			if (Guide3DRenderer.rotateY <= 1) {
				Guide3DRenderer.rotateY = 360;
			}
			break;
		case 5:
			Guide3DRenderer.rotateY = 0;
			Guide3DRenderer.rotate = 180;
			break;

		}
	}

	@Override
	public String getDisplayName() {
		return FontHelper.translate("guide.ExampleConfig.name");
	}

	@Override
	public ArrayList<GuidePageInfo> getPageInfo(ArrayList<GuidePageInfo> pageInfo) {
		pageInfo.add(new GuidePageInfo("guide.FurnaceProgress.name", new String[0]));
		pageInfo.add(new GuidePageInfo("guide.ChestMonitoring.name", new String[0]).setRequiresNewPage());
		return pageInfo;
	}

	public ArrayList<IGuidePageElement> getElements(ArrayList<IGuidePageElement> elements) {
		elements.add(new Guide3DRenderer(FurnaceProgress.instance2, 0, 48, 4, 16, 80, 120));
		elements.add(new Guide3DRenderer(new InventoryExample(), 1, 48, 4, 16, 80, 120));
		elements.add(new Guide3DRenderer(new ExampleConfigurations.WirelessRedstone(), 2, 32, 4, 16, 160, 120));
		return elements;
	}

}
