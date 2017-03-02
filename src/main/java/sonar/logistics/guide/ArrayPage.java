package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.logistics.LogisticsItems;

public class ArrayPage extends BaseItemPage implements IGuidePage {

	public ArrayPage(int pageID) {
		super(pageID, new ItemStack(LogisticsItems.partArray));
	}
	
	@Override
	public int getPageCount() {
		return 1;
	}

}
