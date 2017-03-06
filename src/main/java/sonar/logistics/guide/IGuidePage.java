package sonar.logistics.guide;

import java.util.ArrayList;

import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.elements.ElementInfo;

public interface IGuidePage {
	
	public void initGui(GuiGuide gui, int subPage);

	public void mouseClicked(GuiGuide gui, int x, int y, int button);
	
	public void drawPageInGui(GuiGuide gui, int y);
	
	public void drawPage(GuiGuide gui, int x, int y, int page);
	
	public void drawForegroundPage(GuiGuide gui, int x, int y, int page);
	
	public void drawBackgroundPage(GuiGuide gui, int x, int y, int page);

	public int getLineWidth(int linePos, int page);

	public int getPageCount();
	
	public int pageID();
	
	public String getDisplayName();
	
	public ArrayList<ElementInfo> getPageInfo(GuiGuide gui, ArrayList<ElementInfo> pageInfo);

	public ArrayList<IGuidePageElement> getElements(GuiGuide gui, ArrayList<IGuidePageElement> elements);	
	
}
