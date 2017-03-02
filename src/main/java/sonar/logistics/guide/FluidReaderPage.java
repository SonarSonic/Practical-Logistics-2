package sonar.logistics.guide;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class FluidReaderPage extends BaseItemPage implements IGuidePage {

	public FluidReaderPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.fluidReaderPart));
	}

	public ArrayList<GuidePageInfo> getPageInfo(ArrayList<GuidePageInfo> pageInfo) {
		pageInfo.add(new GuidePageInfo("guide." + unlocalizedName.substring(5) + ".name", new String[0]));
		pageInfo.add(new GuidePageInfo("guide." + "ChannelsSTART" + ".name", new String[0]));
		pageInfo.add(new GuidePageInfo("guide." + "ChannelsUNLIMITED" + ".name", new String[0]));
		pageInfo.add(new GuidePageInfo("guide." + "ChannelsEND" + ".name", new String[0]));		
		return pageInfo;
	}

}
