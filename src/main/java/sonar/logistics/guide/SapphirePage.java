package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class SapphirePage extends BaseItemPage implements IGuidePage {

	public SapphirePage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.sapphire));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
