package sonar.logistics.guide.pages;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsBlocks;
import sonar.logistics.LogisticsConfig;
import sonar.logistics.LogisticsItems;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.BaseItemPage;
import sonar.logistics.guide.IGuidePage;
import sonar.logistics.guide.elements.ElementInfo;

public class SapphireOrePage extends BaseItemPage implements IGuidePage {

	public SapphireOrePage(int pageID) {
		super(pageID, new ItemStack(LogisticsBlocks.sapphire_ore));
	}

	public ArrayList<ElementInfo> getPageInfo(GuiGuide gui, ArrayList<ElementInfo> pageInfo) {
		pageInfo.add(new ElementInfo("guide." + unlocalizedName.substring(5) + ".name", getAdditionals()));
		return pageInfo;
	}

	public String[] getAdditionals() {
		return new String[] { String.valueOf(LogisticsConfig.sapphireMinY), String.valueOf(LogisticsConfig.sapphireMaxY) };
	}
}
