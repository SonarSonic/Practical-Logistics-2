package sonar.logistics.guide;

import java.util.List;

import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.elements.ElementInfo;

public interface IGuidePage {
	
	public void initGui(GuiGuide gui, int subPage);

	public void mouseClicked(GuiGuide gui, int x, int y, int button);
	
	public void drawPageInGui(GuiGuide gui, int y);
	
	public void drawPage(GuiGuide gui, int x, int y, int page);
	
	public void drawForegroundPage(GuiGuide gui, int x, int y, int page, float partialTicks);
	
	public void drawBackgroundPage(GuiGuide gui, int x, int y, int page);

	public int getLineWidth(int linePos, int page);

	public int getPageCount();
	
	public int pageID();
	
	public String getDisplayName();
	
	public List<ElementInfo> getPageInfo(GuiGuide gui, List<ElementInfo> pageInfo);

	public List<IGuidePageElement> getElements(GuiGuide gui, List<IGuidePageElement> elements);	
	
}
