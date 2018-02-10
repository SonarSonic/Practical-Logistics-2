package sonar.logistics.helpers;

import java.util.List;

import sonar.logistics.api.displays.elements.DisplayElementList;
import sonar.logistics.api.displays.elements.TextDisplayElement;
import sonar.logistics.client.gsi.IGSI;

public class DisplayElementHelper {

	public DisplayElementList createTextElementList(List<String> text, double width, double height, double maxScale){
		DisplayElementList list = new DisplayElementList(width, height, maxScale);
		for(String t : text){
			list.addElement(new TextDisplayElement(list, t));
		}
		return list;
	}
	
}
