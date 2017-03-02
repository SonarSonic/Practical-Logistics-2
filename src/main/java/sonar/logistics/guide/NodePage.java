package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class NodePage extends BaseItemPage implements IGuidePage {

	public NodePage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.partNode));
	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
