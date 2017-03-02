package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class LargeDisplayScreenPage extends BaseItemPage implements IGuidePage {

	public LargeDisplayScreenPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.largeDisplayScreen));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
