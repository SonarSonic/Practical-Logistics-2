package sonar.logistics.api.info;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.text.StyledInfo;
import sonar.logistics.api.displays.elements.text.StyledStringLine;
import sonar.logistics.api.displays.elements.text.StyledTitleElement;
import sonar.logistics.api.displays.references.ReferenceType;
import sonar.logistics.api.register.LogicPath;

/** for your info to be registered you must use {@link LogicInfoType} implement this for all types of info */
public interface IInfo<T extends IInfo> extends INBTSyncable {

	/** this must be the same as the ID specified in {@link LogicInfoType} */
    String getID();

	/** if they are identical **/
    boolean isIdenticalInfo(T info);

	/** if they are of the same type with just different values **/
    boolean isMatchingInfo(T info);

	/** if they are of the same type with just different values **/
    boolean isMatchingType(IInfo info);

	default boolean isHeader() {
		return false;
	}

	default void addDefaultElements(IElementStorageHolder h, InfoUUID uuid) {
		List<IDisplayElement> elements = new ArrayList<>();
		createDefaultElements(elements, h, uuid);
		elements.forEach(e -> h.getElements().addElement(e));
	}

	default void createDefaultElements(List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		doCreateDefaultElements(this, toAdd, h, uuid);
	}

	static void doCreateDefaultElements(IInfo info, List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		StyledTitleElement element = new StyledTitleElement();
		StyledStringLine line1 = new StyledStringLine(element), line2 = new StyledStringLine(element);
		line1.setStrings(Lists.newArrayList(new StyledInfo(uuid, ReferenceType.IDENTIFIER)));
		if (info instanceof ISuffixable) {
			line2.setStrings(Lists.newArrayList(new StyledInfo(uuid, ReferenceType.PREFIX), new StyledInfo(uuid, ReferenceType.RAW_INFO), new StyledInfo(uuid, ReferenceType.SUFFIX)));
		} else {
			line2.setStrings(Lists.newArrayList(new StyledInfo(uuid, ReferenceType.CLIENT_INFO)));
		}
		element.setLines(Lists.newArrayList(line1, line2));
		toAdd.add(element);
	}

	boolean isValid();

	LogicPath getPath();

	T setPath(LogicPath path);

	/** it is essential that you copy the LogicPath also */
    T copy();

	void identifyChanges(T newInfo);

	void onInfoStored();

}
