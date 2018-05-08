package sonar.logistics.core.items.guide.pages.pages;

import sonar.logistics.core.items.guide.GuiGuide;
import sonar.logistics.core.items.guide.pages.elements.ElementInfo;
import sonar.logistics.core.items.guide.pages.elements.IGuidePageElement;

import java.util.List;

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
