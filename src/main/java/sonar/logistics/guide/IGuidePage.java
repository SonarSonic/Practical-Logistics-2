package sonar.logistics.guide;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;

public interface IGuidePage {

	public ItemStack getItemStack();
	
	public void drawPage(GuiSonar gui, int x, int y, int page);

	public int getPageCount();
	
}
