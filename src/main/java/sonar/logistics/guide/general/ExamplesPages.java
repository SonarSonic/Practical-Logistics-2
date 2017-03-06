package sonar.logistics.guide.general;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.FontHelper;
import sonar.logistics.Logistics;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.client.gui.LogisticsButton;
import sonar.logistics.guide.BaseInfoPage;
import sonar.logistics.guide.ExampleConfigurations;
import sonar.logistics.guide.ExampleConfigurations.FurnaceProgress;
import sonar.logistics.guide.ExampleConfigurations.InventoryExample;
import sonar.logistics.guide.IGuidePageElement;
import sonar.logistics.guide.elements.Element3DRenderer;
import sonar.logistics.guide.elements.ElementImage;
import sonar.logistics.guide.elements.ElementInfo;

public class ExamplesPages extends BaseInfoPage {

	public ExamplesPages(int pageID) {
		super(pageID);
	}

	@Override
	public String getDisplayName() {
		return FontHelper.translate("guide.ExampleConfig.name");
	}

	//// CREATE \\\\

	@Override
	public ArrayList<ElementInfo> getPageInfo(GuiGuide gui, ArrayList<ElementInfo> pageInfo) {
		pageInfo.add(new ElementInfo("guide.FurnaceProgress.name", new String[0]));
		pageInfo.add(new ElementInfo("guide.ChestMonitoring.name", new String[0]).setRequiresNewPage());
		pageInfo.add(new ElementInfo("guide.WirelessRedstone.name", new String[0]).setRequiresNewPage());
		return pageInfo;
	}

	public ArrayList<IGuidePageElement> getElements(GuiGuide gui, ArrayList<IGuidePageElement> elements) {
		elements.add(new Element3DRenderer(FurnaceProgress.instance2, 0, 48, 4, 16, 80, 120));
		elements.add(new Element3DRenderer(new InventoryExample(), 1, 48, 4, 16, 80, 120));
		elements.add(new Element3DRenderer(new ExampleConfigurations.WirelessRedstone(), 2, 32, 4, 16, 120, 120));
		return elements;
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
			Element3DRenderer.canRotate = !Element3DRenderer.canRotate;
			break;
		case 1:
			Element3DRenderer.rotateY += 10;
			if (Element3DRenderer.rotateY >= 360) {
				Element3DRenderer.rotateY = 0;
			}
			break;
		case 2:
			Element3DRenderer.rotate -= 10;
			if (Element3DRenderer.rotate <= 1) {
				Element3DRenderer.rotate = 360;
			}
			break;
		case 3:
			Element3DRenderer.rotate += 10;
			if (Element3DRenderer.rotate >= 360) {
				Element3DRenderer.rotate = 0;
			}
			break;
		case 4:
			Element3DRenderer.rotateY -= 10;
			if (Element3DRenderer.rotateY <= 1) {
				Element3DRenderer.rotateY = 360;
			}
			break;
		case 5:
			Element3DRenderer.rotateY = 0;
			Element3DRenderer.rotate = 180;
			break;

		}
	}
}