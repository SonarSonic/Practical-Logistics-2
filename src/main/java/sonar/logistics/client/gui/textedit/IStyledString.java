package sonar.logistics.client.gui.textedit;

public interface IStyledString {

	/**the formatting of this string*/
	public SonarStyling setStyle(SonarStyling formatting);
	
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
	
	public boolean canCombine(StyledString ss);
	
	public void combine(StyledString ss);
	
}
