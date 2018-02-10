package sonar.logistics.helpers;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.TextDisplayElement;
import sonar.logistics.api.info.IInfo;

public class DisplayElementHelper {

	public DisplayElementContainer createTextElementList(List<String> text, double width, double height, double maxScale) {
		DisplayElementContainer list = new DisplayElementContainer(width, height, maxScale);
		for (String t : text) {
			list.addElement(new TextDisplayElement(list, t));
		}
		return list;
	}

	public static int getRegisteredID(IDisplayElement info){
		return PL2ASMLoader.elementIDs.get(info.getRegisteredName());
	}
	
	public static Class<? extends IDisplayElement> getElementClass(int id) {
		return PL2ASMLoader.elementIClasses.get(id);
	}

	public static NBTTagCompound saveElement(NBTTagCompound tag, IDisplayElement info, SyncType type) {
		tag.setInteger("EiD", getRegisteredID(info));
		return info.writeData(tag, type);
	}

	public static IDisplayElement loadElement(NBTTagCompound tag) {
		int elementID = tag.getInteger("EiD");
		return NBTHelper.instanceNBTSyncable(getElementClass(elementID), tag);
	}

	public static void addDefaultElements(IInfo info, List<IDisplayElement> elements){
		elements.add(e);
		
	}
	
}
