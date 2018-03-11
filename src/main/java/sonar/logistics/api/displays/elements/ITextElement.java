package sonar.logistics.api.displays.elements;

import java.util.List;

import net.minecraft.util.text.TextFormatting;
import sonar.logistics.api.displays.elements.text.StyledStringLine;
import sonar.logistics.api.displays.elements.text.TextSelectionType;

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
