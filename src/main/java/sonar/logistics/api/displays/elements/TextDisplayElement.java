package sonar.logistics.api.displays.elements;

import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.IDisplayElementList;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.helpers.InfoRenderer;

public class TextDisplayElement extends AbstractDisplayElement {

	protected String unformattedText;

	public TextDisplayElement(IDisplayElementList list, String unformattedText) {
		super(list);
		this.unformattedText = unformattedText;
	}

	@Override
	int[] createUnscaledWidthHeight() {
		return new int[]{InfoRenderer.getStringWidth(unformattedText), InfoRenderer.getStringHeight()};
	}
	
	@Override
	public String getRepresentiveString() {
		return unformattedText;
	}
}
