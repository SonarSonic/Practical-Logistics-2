package sonar.logistics.core.items.guide.pages.pages;

import sonar.core.helpers.FontHelper;
import sonar.logistics.core.items.guide.GuiGuide;
import sonar.logistics.core.items.guide.pages.elements.ElementInfo;

import java.util.List;

public class GeneralPage extends BaseInfoPage {
	public String displayKey;
	public String[] descKey;

	public GeneralPage(int pageID, String displayKey, String... descKey) {
		super(pageID);
		this.displayKey = displayKey;
		this.descKey = descKey;
	}

	@Override
	public String getDisplayName() {
		return FontHelper.translate(displayKey);
	}

	@Override
	public List<ElementInfo> getPageInfo(GuiGuide gui, List<ElementInfo> pageInfo) {
		for (String desc : descKey)
			pageInfo.add(new ElementInfo(desc, new String[0]));
		return pageInfo;
	}
}
