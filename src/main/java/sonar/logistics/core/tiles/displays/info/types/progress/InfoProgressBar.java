package sonar.logistics.core.tiles.displays.info.types.progress;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.*;
import sonar.logistics.api.core.tiles.displays.info.comparators.ComparableObject;
import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IElementStorageHolder;
import sonar.logistics.core.tiles.displays.info.types.general.LogicInfo;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;

import java.util.ArrayList;
import java.util.List;

@ASMInfo(id = InfoProgressBar.id, modid = PL2Constants.MODID)
public class InfoProgressBar implements IInfo<InfoProgressBar>, INBTSyncable, INameableInfo<InfoProgressBar>, ISuffixable, IComparableInfo<InfoProgressBar> {

	public static final String id = "progress";
	public LogicInfo first, second;
	public int compare;
	public double firstNum, secondNum;

	public InfoProgressBar() {}

	public InfoProgressBar(IInfo first, IInfo second) {
		this.first = (LogicInfo) first;
		this.second = (LogicInfo) second;
		checkInfo();
	}

	public static boolean isStorableInfo(IInfo info) {
		return info instanceof LogicInfo;
	}

	public void checkInfo() {
		if (isValid() && first.isValid() && second.isValid() &&  first.getInfoType().isNumber()&& second.getInfoType().isNumber()) {
			//TODO this should be available to us as numbers already somehow
			firstNum = Double.valueOf(first.getInfo().toString());
			secondNum = Double.valueOf(second.getInfo().toString());
			compare = Double.compare(firstNum, secondNum);
		}
	}

	@Override
	public String getClientIdentifier() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getClientIdentifier();
	}

	@Override
	public String getRawData() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getRawData();
	}

	@Override
	public String getClientObject() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getClientObject(); // + "/" + (compare != 1 ? second : first).getClientObject();
	}

	@Override
	public String getClientType() {
		return "Progress";
	}
	@Override
	public String getSuffix() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getSuffix();
	}

	@Override
	public String getPrefix() {
		if (!isValid()) {
			return "ERROR";
		}
		return (compare == 1 ? second : first).getPrefix();
	}

	@Override
	public void createDefaultElements(List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		IInfo.doCreateDefaultElements(this, toAdd, h, uuid);
		toAdd.add(new ElementProgressBar(uuid));
	}


	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		first = (LogicInfo) InfoHelper.loadInfo(InfoHelper.getName(LogicInfo.id), nbt.getCompoundTag("first"));
		second = (LogicInfo) InfoHelper.loadInfo(InfoHelper.getName(LogicInfo.id), nbt.getCompoundTag("second"));
		checkInfo();
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setTag("first", InfoHelper.writeInfoToNBT(new NBTTagCompound(), first, type));
		nbt.setTag("second", InfoHelper.writeInfoToNBT(new NBTTagCompound(), second, type));
		return nbt;
	}

	@Override
	public boolean isIdenticalInfo(InfoProgressBar info) {
		return info.first.isIdenticalInfo(first) && info.second.isIdenticalInfo(second);
	}

	@Override
	public boolean isMatchingInfo(InfoProgressBar info) {
		return info.first.isMatchingInfo(first) && info.second.isMatchingInfo(second);
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof InfoProgressBar;
	}

	@Override
	public boolean isHeader() {
		return false;
	}

	@Override
	public boolean isValid() {
		return first != null && second != null && first.isValid() && second.isValid();
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public InfoProgressBar copy() {
		return new InfoProgressBar(first.copy(), second.copy());
	}

	@Override
	public void identifyChanges(InfoProgressBar newInfo) {
		first.identifyChanges(newInfo.first);
		second.identifyChanges(newInfo.second);
	}

	@Override
	public LogicPath getPath() {
		return null;
	}

	@Override
	public InfoProgressBar setPath(LogicPath path) {
		return this;
	}
	
	@Override
	public void onInfoStored() {
		first.onInfoStored();
		second.onInfoStored();
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		if (first != null) {
			List<ComparableObject> firstObj = first.getComparableObjects(new ArrayList<>());
			firstObj.forEach(obj -> objects.add(new ComparableObject(this, "First: " + obj.string, obj.object)));
		}
		if (second != null) {
			List<ComparableObject> firstObj = second.getComparableObjects(new ArrayList<>());
			firstObj.forEach(obj -> objects.add(new ComparableObject(this, "Second: " + obj.string, obj.object)));
		}
		return objects;
	}

}