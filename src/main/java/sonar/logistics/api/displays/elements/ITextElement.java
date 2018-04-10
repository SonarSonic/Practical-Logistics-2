package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.elements.text.StyledStringLine;

public interface ITextElement extends IDisplayElement {

	/*
	void setSelectedRange(double[] start, double end[], TextSelectionType type);
	
	void enableSpecialFormatting(List<TextFormatting> formatting);

	void disableSpecialFormatting(List<TextFormatting> formatting);
	
	void setFontColour(int colour);
	
	void setBackgroundColour(int colour);
	
	*/
	StyledStringLine getStyledStringCompound();
}
