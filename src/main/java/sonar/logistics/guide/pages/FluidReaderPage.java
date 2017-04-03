package sonar.logistics.guide.pages;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import sonar.logistics.PL2Items;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.BaseItemPage;
import sonar.logistics.guide.IGuidePage;
import sonar.logistics.guide.elements.ElementInfo;

public class FluidReaderPage extends BaseItemPage implements IGuidePage {

	public FluidReaderPage(int pageID) {
		super(pageID, new ItemStack(PL2Items.fluid_reader));
	}

	public ArrayList<ElementInfo> getPageInfo(GuiGuide gui, ArrayList<ElementInfo> pageInfo) {
		pageInfo.add(new ElementInfo("guide." + unlocalizedName.substring(5) + ".name", new String[0]));
		pageInfo.add(new ElementInfo("guide." + "ChannelsSTART" + ".name", new String[0]));
		pageInfo.add(new ElementInfo("guide." + "ChannelsUNLIMITED" + ".name", new String[0]));
		pageInfo.add(new ElementInfo("guide." + "ChannelsEND" + ".name", new String[0]));		
		return pageInfo;
	}

}
