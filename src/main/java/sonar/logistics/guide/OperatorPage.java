package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class OperatorPage extends BaseItemPage implements IGuidePage {

	public OperatorPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.operator));
	}

}
