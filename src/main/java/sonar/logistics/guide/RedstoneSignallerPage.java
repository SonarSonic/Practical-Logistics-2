package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class RedstoneSignallerPage extends BaseItemPage implements IGuidePage {

	public RedstoneSignallerPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.partRedstoneSignaller));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
