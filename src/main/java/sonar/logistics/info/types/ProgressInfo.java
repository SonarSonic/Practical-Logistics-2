package sonar.logistics.info.types;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.elements.IDisplayElement;
import sonar.logistics.api.displays.elements.IElementStorageHolder;
import sonar.logistics.api.displays.elements.types.NetworkFluidElement;
import sonar.logistics.api.displays.elements.types.ProgressBarElement;
import sonar.logistics.api.info.IComparableInfo;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.INameableInfo;
import sonar.logistics.api.info.ISuffixable;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.tiles.signaller.ComparableObject;
import sonar.logistics.networking.info.InfoHelper;

@LogicInfoType(id = ProgressInfo.id, modid = PL2Constants.MODID)
public class ProgressInfo implements IInfo<ProgressInfo>, INBTSyncable, INameableInfo<ProgressInfo>, ISuffixable, IComparableInfo<ProgressInfo> {

	public static final String id = "progress";
	public LogicInfo first, second;
	public int compare;
	public double firstNum, secondNum;

	public ProgressInfo() {}

	public ProgressInfo(IInfo first, IInfo second) {
		this.first = (LogicInfo) first;
		this.second = (LogicInfo) second;
		checkInfo();
	}

	public static boolean isStorableInfo(IInfo info) {
		return info != null && info instanceof LogicInfo;
	}

	public void checkInfo() {
		if (isValid() && first.getInfoType().isNumber() && second.getInfoType().isNumber()) {
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
		toAdd.add(new ProgressBarElement(uuid));
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
	public boolean isIdenticalInfo(ProgressInfo info) {
		return info.first.isIdenticalInfo(first) && info.second.isIdenticalInfo(second);
	}

	@Override
	public boolean isMatchingInfo(ProgressInfo info) {
		return info.first.isMatchingInfo(first) && info.second.isMatchingInfo(second);
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof ProgressInfo;
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
	public ProgressInfo copy() {
		return new ProgressInfo(first.copy(), second.copy());
	}

	@Override
	public void identifyChanges(ProgressInfo newInfo) {
		first.identifyChanges(newInfo.first);
		second.identifyChanges(newInfo.second);
	}

	@Override
	public LogicPath getPath() {
		return null;
	}

	@Override
	public ProgressInfo setPath(LogicPath path) {
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
			List<ComparableObject> firstObj = first.getComparableObjects(Lists.newArrayList());
			firstObj.forEach(obj -> objects.add(new ComparableObject(this, "First: " + obj.string, obj.object)));
		}
		if (second != null) {
			List<ComparableObject> firstObj = second.getComparableObjects(Lists.newArrayList());
			firstObj.forEach(obj -> objects.add(new ComparableObject(this, "Second: " + obj.string, obj.object)));
		}
		return objects;
	}

}