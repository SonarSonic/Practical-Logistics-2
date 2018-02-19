package sonar.logistics.api.info;

import java.util.List;

import com.google.common.collect.Lists;

import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.InfoReference;
import sonar.logistics.api.displays.ReferenceType;
import sonar.logistics.api.displays.elements.CompoundTextElement;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.DisplayElementList;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.register.LogicPath;

/** for your info to be registered you must use {@link LogicInfoType} implement this for all types of info */
public interface IInfo<T extends IInfo> extends INBTSyncable {

	/** this must be the same as the ID specified in {@link LogicInfoType} */
	public String getID();

	/** if they are identical **/
	public boolean isIdenticalInfo(T info);

	/** if they are of the same type with just different values **/
	public boolean isMatchingInfo(T info);

	/** if they are of the same type with just different values **/
	public boolean isMatchingType(IInfo info);

	public default boolean isHeader() {
		return false;
	}

	public default void addDefaultElements(IElementStorageHolder h, InfoUUID uuid) {
		List<IDisplayElement> elements = Lists.newArrayList();
		createDefaultElements(elements, h, uuid);
		elements.forEach(e -> h.getElements().addElement(e));
	}

	public default void createDefaultElements(List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		doCreateDefaultElements(this, toAdd, h, uuid);
	}

	public static void doCreateDefaultElements(IInfo info, List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		DisplayElementList list = new DisplayElementList(h);
		CompoundTextElement first = new CompoundTextElement(CompoundTextElement.REF);
		CompoundTextElement second = new CompoundTextElement(CompoundTextElement.REF + " " + CompoundTextElement.REF + " " + CompoundTextElement.REF);
		List<InfoReference> firstRefs = Lists.newArrayList(), secondRefs = Lists.newArrayList();
		firstRefs.add(new InfoReference(uuid, ReferenceType.IDENTIFIER, -1));
		secondRefs.add(new InfoReference(uuid, ReferenceType.PREFIX, -1));
		secondRefs.add(new InfoReference(uuid, ReferenceType.RAW_INFO, -1));
		secondRefs.add(new InfoReference(uuid, ReferenceType.SUFFIX, -1));
		first.setReferences(firstRefs);
		second.setReferences(secondRefs);
		list.getElements().addElement(first);
		list.getElements().addElement(second);
		toAdd.add(list);
	}

	public boolean isValid();

	public LogicPath getPath();

	public T setPath(LogicPath path);

	/** it is essential that you copy the LogicPath also */
	public T copy();

	/* @Deprecated public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos);
	 * @Deprecated public void renderSizeChanged(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos); */
	public void identifyChanges(T newInfo);

	public void onInfoStored();

}
