package sonar.logistics.guide;

import java.util.List;

import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.elements.ElementInfo;

public interface IGuidePage {
	
	void initGui(GuiGuide gui, int subPage);

	void mouseClicked(GuiGuide gui, int x, int y, int button);
	
	void drawPageInGui(GuiGuide gui, int y);
	
	void drawPage(GuiGuide gui, int x, int y, int page);
	
	void drawForegroundPage(GuiGuide gui, int x, int y, int page, float partialTicks);
	
	void drawBackgroundPage(GuiGuide gui, int x, int y, int page);

	int getLineWidth(int linePos, int page);

	int getPageCount();
	
	int pageID();
	
	String getDisplayName();
	
	List<ElementInfo> getPageInfo(GuiGuide gui, List<ElementInfo> pageInfo);

	List<IGuidePageElement> getElements(GuiGuide gui, List<IGuidePageElement> elements);
	
}
