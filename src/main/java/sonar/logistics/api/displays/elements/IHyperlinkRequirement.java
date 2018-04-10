package sonar.logistics.api.displays.elements;

public interface IHyperlinkRequirement {
	
	String getHyperlink();
	
	void onGuiClosed(String hyperlink);	
}
