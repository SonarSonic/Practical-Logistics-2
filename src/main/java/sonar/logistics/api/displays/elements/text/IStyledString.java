package sonar.logistics.api.displays.elements.text;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Holder;

import net.minecraft.util.Tuple;
import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.InfoUUID;

public interface IStyledString extends INBTSyncable {

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
	
	public int getStringLength();
	
	public int getStringWidth();
	
	public boolean canCombine(IStyledString ss);
	
	public void combine(IStyledString ss);
	
	public IStyledString copy();
	
	public String getRegisteredName();

	public void updateTextContents();

	public void updateTextScaling();
	
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
