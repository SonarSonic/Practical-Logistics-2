package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class InventoryReaderPage implements IGuidePage {

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.inventoryReaderPart);
	}

	@Override
	public void drawPage(GuiSonar gui, int x, int y, int page) {
		switch (page) {
		case 0:
			//FontHelper.text(info, x, y, 0);
			break;
		}

	}

	@Override
	public int getPageCount() {
		return 1;
	}

}
