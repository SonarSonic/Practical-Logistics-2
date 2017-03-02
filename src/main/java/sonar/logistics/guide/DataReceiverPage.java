package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class DataReceiverPage extends BaseItemPage implements IGuidePage {

	public DataReceiverPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.partReceiver));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
