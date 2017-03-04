package sonar.logistics.guide.pages;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsBlocks;
import sonar.logistics.LogisticsItems;
import sonar.logistics.guide.BaseItemPage;
import sonar.logistics.guide.IGuidePage;

public class ForgingHammerPage extends BaseItemPage implements IGuidePage {

	public ForgingHammerPage(int pageID) {
		super(pageID, new ItemStack(LogisticsBlocks.hammer));
	}

}
