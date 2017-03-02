package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class StonePlatePage extends BaseItemPage implements IGuidePage {

	public StonePlatePage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.stone_plate));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
