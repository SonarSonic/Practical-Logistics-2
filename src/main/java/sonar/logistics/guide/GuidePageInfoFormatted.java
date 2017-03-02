package sonar.logistics.guide;

import java.util.ArrayList;
import java.util.List;

public class GuidePageInfoFormatted {

	public int order;
	public GuidePageInfo source;
	public List<String> formattedList;
	public ArrayList<GuidePageLink> links;
	public int displayX, displayY;

	public GuidePageInfoFormatted(int order, GuidePageInfo source, List<String> formattedList, ArrayList<GuidePageLink> links) {
		this.order = order;
		this.source = source;
		this.formattedList = formattedList;
		this.links = links;
	}

	public GuidePageInfoFormatted setDisplayPosition(int x, int y) {
		this.displayX = x;
		this.displayY = y;
		return this;
	}

}
