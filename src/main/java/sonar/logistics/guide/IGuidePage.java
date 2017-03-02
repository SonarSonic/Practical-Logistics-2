package sonar.logistics.guide;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.utils.Pair;
import sonar.logistics.client.gui.GuiGuide;

public interface IGuidePage {
	
	public void initGui(GuiGuide gui, int subPage);

	public void mouseClicked(GuiGuide gui, int x, int y, int button);
	
	public void drawPageInGui(GuiGuide gui, int y);
	
	public void drawPage(GuiGuide gui, int x, int y, int page);
	
	public int getLineWidth(int linePos);

	public int getPageCount();
	
	public int pageID();
	
	public String getDisplayName();
	
	public ArrayList<GuidePageInfo> getPageInfo(ArrayList<GuidePageInfo> pageInfo);

	public ArrayList<IGuidePageElement> getElements(ArrayList<IGuidePageElement> elements);	
	
}
