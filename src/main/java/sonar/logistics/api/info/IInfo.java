package sonar.logistics.api.info;

import java.util.List;

import com.google.common.collect.Lists;

import sonar.core.api.nbt.INBTSyncable;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.types.StyledTextElement;
import sonar.logistics.api.displays.references.InfoReference;
import sonar.logistics.api.displays.references.ReferenceType;
import sonar.logistics.api.displays.storage.DisplayElementContainer;
import sonar.logistics.api.displays.storage.DisplayElementList;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.client.gui.textedit.StyledInfo;
import sonar.logistics.client.gui.textedit.StyledStringLine;

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
		StyledTextElement element = new StyledTextElement();
		StyledStringLine line1 = new StyledStringLine(element), line2 = new StyledStringLine(element);
		line1.setStrings(Lists.newArrayList(new StyledInfo(uuid, ReferenceType.IDENTIFIER)));
		line2.setStrings(Lists.newArrayList(new StyledInfo(uuid, ReferenceType.PREFIX), new StyledInfo(uuid, ReferenceType.RAW_INFO), new StyledInfo(uuid, ReferenceType.SUFFIX)));
		element.setLines(Lists.newArrayList(line1, line2));
		toAdd.add(element);
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
