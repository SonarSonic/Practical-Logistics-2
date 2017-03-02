package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class EntityTransceiverPage extends BaseItemPage implements IGuidePage {

	public EntityTransceiverPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.entityTransceiver));
	}
	
	@Override
	public int getPageCount() {
		return 1;
	}

}
