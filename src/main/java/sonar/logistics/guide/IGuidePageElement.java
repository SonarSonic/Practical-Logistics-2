package sonar.logistics.guide;

import sonar.logistics.client.gui.GuiGuide;

public interface IGuidePageElement {

	public int getDisplayPage();
	/**left, top, width, height*/
	public int[] getSizing();

	public void drawElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY);
	
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY);
	
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY);
	
	public boolean mouseClicked(GuiGuide gui, IGuidePage page, int x, int y, int button);
}
