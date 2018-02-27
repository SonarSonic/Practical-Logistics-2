package sonar.logistics.client.gui.textedit;

public interface ILineCounter {

	public int getLineCount();
	
	public int getLineLength(int line);
	
	public int getLineWidth(int line);
	
	public String getUnformattedLine(int line);
	
	public String getFormattedLine(int line);	

	public StyledStringLine getLine(int line);
	
}
