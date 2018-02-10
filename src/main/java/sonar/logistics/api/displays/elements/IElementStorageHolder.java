package sonar.logistics.api.displays.elements;

import sonar.logistics.api.displays.IDisplayElement;

public interface IElementStorageHolder {

	public ElementStorage getElements();

	public void onElementAdded(IDisplayElement element);

	public void onElementRemoved(IDisplayElement element);

	public void onElementChanged(IDisplayElement element);
	
}
