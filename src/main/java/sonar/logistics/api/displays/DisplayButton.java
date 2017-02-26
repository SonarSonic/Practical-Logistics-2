package sonar.logistics.api.displays;

public class DisplayButton {

	public int texX, texY;	
	public String hoverString;
	
	public DisplayButton(String id, int texX, int texY, String hoverString){
		this.texX = texX;
		this.texY = texY;
		this.hoverString = hoverString;
	}
	
}
