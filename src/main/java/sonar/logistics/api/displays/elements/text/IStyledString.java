package sonar.logistics.api.displays.elements.text;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;

import net.minecraft.util.Tuple;
import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;

/**a styled string contains a String to be displayed and it's specific style*/
public interface IStyledString extends INBTSyncable {
	
	public String getRegisteredName();
	
	public Tuple<Character, Integer> getCharClicked(int yPos, Holder<Double> subClickX, Holder<Double> subClickY);
	
	public IStyledString setLine(StyledStringLine line);
	
	public StyledStringLine getLine();
	
	/**the formatting of this string*/
	public SonarStyling setStyle(SonarStyling formatting);
	
	public void onStyleChanged();
	
	/**the formatting of this string*/
	public SonarStyling getStyle();
	
	/**the unformatted version of the string*/
	public String setUnformattedString(String s);	

	/**the unformatted version of the string*/
	public String getUnformattedString();	

	/**the formatted version of the string*/
	public String getFormattedString();

	/**the formating of the string in the form a string*/
	public String getTextFormattingStyle();
	
	/**the index length of the string to render*/
	public int getStringLength();
	
	/** the measured render width of the string in pixels*/
	public int getStringWidth();
	
	/** used in saving {@link IStyledString}
	 * @return returns true if the given string has matching styling and can be conbined with this one. */
	public boolean canCombine(IStyledString ss);
	
	/**adds the string to the end of this one, 
	 * this is called only if {@link #canCombine(IStyledString)} is true*/
	public void combine(IStyledString ss);
	
	/**creates a new deep copy of the {@link IStyledString}*/
	public IStyledString copy();

	public void updateTextContents();

	public void updateTextScaling();

	/**all the {@link InfoUUID} which are required to render this {@link IStyledString},
	 * this information is used to know which {@link IInfo} should be synced with the client*/
	public default List<InfoUUID> getInfoReferences(){
		return new ArrayList<>();
	}
	
	public default StyledTextElement getText(){
		return getLine().getText();
	}
	
	public default DisplayGSI getGSI(){
		return getLine().getText().getGSI();
	}
}
