package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class DataEmitterPage extends BaseItemPage implements IGuidePage {

	public DataEmitterPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.partEmitter));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
