package sonar.logistics.guide.elements;

import java.util.ArrayList;
import java.util.List;

public class ElementInfoFormatted {

	public int order;
	public ElementInfo source;
	public List<String> formattedList;
	public ArrayList<ElementLink> links;
	public int displayX, displayY;

	public ElementInfoFormatted(int order, ElementInfo source, List<String> formattedList, ArrayList<ElementLink> links) {
		this.order = order;
		this.source = source;
		this.formattedList = formattedList;
		this.links = links;
	}

	public ElementInfoFormatted setDisplayPosition(int x, int y) {
		this.displayX = x;
		this.displayY = y;
		return this;
	}

}
