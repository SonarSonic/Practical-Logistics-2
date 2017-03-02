package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class DisplayScreenPage extends BaseItemPage implements IGuidePage {

	public DisplayScreenPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.displayScreen));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
