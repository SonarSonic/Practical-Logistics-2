package sonar.logistics.guide;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsBlocks;
import sonar.logistics.LogisticsConfig;
import sonar.logistics.LogisticsItems;

public class SapphireOrePage extends BaseItemPage implements IGuidePage {

	public SapphireOrePage(int pageID) {
		super(pageID, new ItemStack(LogisticsBlocks.sapphire_ore));
	}

	public ArrayList<GuidePageInfo> getPageInfo(ArrayList<GuidePageInfo> pageInfo) {
		pageInfo.add(new GuidePageInfo("guide." + unlocalizedName.substring(5) + ".name", getAdditionals()));
		return pageInfo;
	}

	public String[] getAdditionals() {
		return new String[] { String.valueOf(LogisticsConfig.sapphireMinY), String.valueOf(LogisticsConfig.sapphireMaxY) };
	}
}
