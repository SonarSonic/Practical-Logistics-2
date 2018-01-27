package sonar.logistics.client.gsi;

public class GSIButton {

	public int buttonID;
	//the x/y position of this button on the filter button texture, (counted in )
	public int buttonX, buttonY;
	public double posX, posY, width, height;
	public String hoverString;

	public GSIButton(int buttonID, double posX, double posY, double width, double height, int buttonX, int buttonY, String hoverString) {
		this.buttonID = buttonID;
		this.posX = posX;
		this.posY = posY;
		this.width = width;
		this.height = height;
		this.buttonX = buttonX;
		this.buttonY = buttonY;
		this.hoverString = hoverString;
	}

	public boolean isClickOver(double clickX, double clickY) {
		return clickX >= posX && clickY >= posY && clickX <= posX + width && clickY <= posY + height;
	}
	
	public void render(){
	}
}
