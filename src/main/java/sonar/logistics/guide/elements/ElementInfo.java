package sonar.logistics.guide.elements;

public class ElementInfo {

	public boolean newPage;
	public String key;
	public String[] additionals;
	//public boolean wrapItem = false;

	public ElementInfo(String key, String[] additionals) {
		this.key = key;
		this.additionals = additionals;
	}
	/*
	public GuidePageInfo setItemWrap() {
		wrapItem = true;
		return this;
	}
	*/
	public ElementInfo setRequiresNewPage() {
		newPage = true;
		return this;
	}

}
