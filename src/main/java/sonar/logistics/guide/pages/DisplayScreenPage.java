package sonar.logistics.guide.pages;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;
import sonar.logistics.guide.BaseItemPage;
import sonar.logistics.guide.IGuidePage;

public class DisplayScreenPage extends BaseItemPage implements IGuidePage {

	public DisplayScreenPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.displayScreen));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
