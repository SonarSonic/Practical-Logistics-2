package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class TransceiverPage extends BaseItemPage implements IGuidePage {

	public TransceiverPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.transceiver));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
