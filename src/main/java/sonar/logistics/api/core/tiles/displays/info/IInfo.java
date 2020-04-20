package sonar.logistics.api.core.tiles.displays.info;

import com.google.common.collect.Lists;
import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IElementStorageHolder;
import sonar.logistics.core.tiles.displays.info.references.ReferenceType;
import sonar.logistics.core.tiles.displays.info.types.text.StyledTitleElement;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledInfo;
import sonar.logistics.core.tiles.displays.info.types.text.styling.StyledStringLine;

import java.util.ArrayList;
import java.util.List;

/** for your info to be registered you must use {@link ASMInfo} implement this for all types of info */
public interface IInfo<T extends IInfo> extends INBTSyncable {

	/** this must be the same as the ID specified in {@link ASMInfo} */
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
