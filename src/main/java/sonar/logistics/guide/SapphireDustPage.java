package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class SapphireDustPage extends BaseItemPage implements IGuidePage {

	public SapphireDustPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.sapphire_dust));
	}
	
	@Override
	public int getPageCount() {
		return 1;
	}

}
