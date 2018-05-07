package sonar.logistics.api.displays.elements.text;

import net.minecraft.util.Tuple;
import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;

import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

/**a styled string contains a String to be displayed and it's specific style*/
public interface IStyledString extends INBTSyncable {
	
	String getRegisteredName();
	
	Tuple<Character, Integer> getCharClicked(int yPos, Holder<Double> subClickX, Holder<Double> subClickY);
	
	IStyledString setLine(StyledStringLine line);
	
	StyledStringLine getLine();
	
	/**the formatting of this string*/
    SonarStyling setStyle(SonarStyling formatting);
	
	void onStyleChanged();
	
	/**the formatting of this string*/
    SonarStyling getStyle();
	
	/**the unformatted version of the string*/
    String setUnformattedString(String s);

	/**the unformatted version of the string*/
    String getUnformattedString();

	/**the formatted version of the string*/
    String getFormattedString();

	/**the formating of the string in the form a string*/
    String getTextFormattingStyle();
	
	/**the index length of the string to render*/
    int getStringLength();
	
	/** the measured render width of the string in pixels*/
    int getStringWidth();
	
	/** used in saving {@link IStyledString}
	 * @return returns true if the given string has matching styling and can be conbined with this one. */
    boolean canCombine(IStyledString ss);
	
	/**adds the string to the end of this one, 
	 * this is called only if {@link #canCombine(IStyledString)} is true*/
    void combine(IStyledString ss);
	
	/**creates a new deep copy of the {@link IStyledString}*/
    IStyledString copy();

	void updateTextContents();

	void updateTextScaling();

	/**all the {@link InfoUUID} which are required to render this {@link IStyledString},
	 * this information is used to know which {@link IInfo} should be synced with the client*/
	default List<InfoUUID> getInfoReferences(){
		return new ArrayList<>();
	}
	
	default StyledTextElement getText(){
		return getLine().getText();
	}
	
	default DisplayGSI getGSI(){
		return getLine().getText().getGSI();
	}
}
