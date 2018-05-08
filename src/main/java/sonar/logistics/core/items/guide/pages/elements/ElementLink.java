package sonar.logistics.core.items.guide.pages.elements;

import com.google.common.base.Objects;
import sonar.logistics.core.items.guide.GuiGuide;
import sonar.logistics.core.items.guide.GuidePageRegistry;
import sonar.logistics.core.items.guide.pages.pages.IGuidePage;

public class ElementLink {
	public int guidePageLink, lineNum, index;
	public int stringWidth;
	public int displayPage;
	public int displayX;
	public int displayY;
	private IGuidePage cached;

	public ElementLink(int guidePageLink, int stringWidth, int lineNum, int index) {
		this.guidePageLink = guidePageLink;
		this.stringWidth = stringWidth;
		this.lineNum = lineNum;
		this.index = index;
	}

	public ElementLink setDisplayPosition(int displayPage, int displayX, int displayY) {
		this.displayPage = displayPage;
		this.displayX = displayX;
		this.displayY = displayY;
		return this;
	}

	public IGuidePage getGuidePage() {
		if(cached!=null){
			return cached;
		}
		return cached = GuidePageRegistry.getGuidePage(guidePageLink);
	}

	public boolean isMouseOver(GuiGuide gui, int x, int y) {
		return x >= displayX && x <= displayX + stringWidth && y >= displayY && y <= displayY + 10;// 12; the font size has been scaled by 0.75
	}

	public int hashCode() {
		return Objects.hashCode(guidePageLink, lineNum, index, stringWidth);
	}

	public boolean equals(Object obj) {
		if (obj instanceof ElementLink) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

}