package sonar.logistics.guide;

public class GuidePageInfo {

	public boolean newPage;
	public String key;
	public String[] additionals;
	//public boolean wrapItem = false;

	public GuidePageInfo(String key, String[] additionals) {
		this.key = key;
		this.additionals = additionals;
	}
	/*
	public GuidePageInfo setItemWrap() {
		wrapItem = true;
		return this;
	}
	*/
	public GuidePageInfo setRequiresNewPage() {
		newPage = true;
		return this;
	}

}
