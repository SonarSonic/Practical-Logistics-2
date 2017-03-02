package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class CablePage extends BaseItemPage implements IGuidePage {

	public CablePage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.partCable));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
