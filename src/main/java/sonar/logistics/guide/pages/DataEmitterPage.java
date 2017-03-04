package sonar.logistics.guide.pages;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;
import sonar.logistics.guide.BaseItemPage;
import sonar.logistics.guide.IGuidePage;

public class DataEmitterPage extends BaseItemPage implements IGuidePage {

	public DataEmitterPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.partEmitter));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
