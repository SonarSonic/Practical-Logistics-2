package sonar.logistics.core.items.guide.pages.pages;

import net.minecraft.item.ItemStack;
import sonar.logistics.PL2Blocks;
import sonar.logistics.core.items.guide.GuiGuide;
import sonar.logistics.core.items.guide.pages.elements.ElementInfo;

import java.util.List;

public class InfoReaderPage extends BaseItemPage implements IGuidePage {

	public InfoReaderPage(int pageID) {
		super(pageID, new ItemStack(PL2Blocks.info_reader));
	}

	public List<ElementInfo> getPageInfo(GuiGuide gui, List<ElementInfo> pageInfo) {
		pageInfo.add(new ElementInfo("guide." + unlocalizedName.substring(5) + ".name", new String[0]));
		pageInfo.add(new ElementInfo("guide." + "ChannelsSTART" + ".name", new String[0]));
		pageInfo.add(new ElementInfo("guide." + "ChannelsSINGLE" + ".name", new String[0]));
		pageInfo.add(new ElementInfo("guide." + "ChannelsEND" + ".name", new String[0]));		
		return pageInfo;
	}

}
