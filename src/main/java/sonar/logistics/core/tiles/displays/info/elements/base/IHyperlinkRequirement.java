package sonar.logistics.core.tiles.displays.info.elements.base;

public interface IHyperlinkRequirement {
	
	String getHyperlink();
	
	void onGuiClosed(String hyperlink);	
}
