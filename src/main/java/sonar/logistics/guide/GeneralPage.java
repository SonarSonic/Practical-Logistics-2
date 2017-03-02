package sonar.logistics.guide;

import java.util.ArrayList;

import sonar.core.helpers.FontHelper;
import sonar.logistics.client.gui.GuiGuide;

public class GeneralPage extends BaseInfoPage {
	public String displayKey;
	public String descKey;

	public GeneralPage(int pageID, String displayKey, String descKey) {
		super(pageID);
		this.displayKey = displayKey;
		this.descKey = descKey;
	}

	@Override
	public String getDisplayName() {
		return FontHelper.translate(displayKey);
	}

	@Override
	public ArrayList<GuidePageInfo> getPageInfo(ArrayList<GuidePageInfo> pageInfo) {
		pageInfo.add(new GuidePageInfo(descKey, new String[0]));
		return pageInfo;
	}
}
