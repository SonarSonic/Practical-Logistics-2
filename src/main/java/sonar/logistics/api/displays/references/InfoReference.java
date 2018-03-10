package sonar.logistics.api.displays.references;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.info.types.LogicInfoList;

public class InfoReference implements INBTSyncable {

	public static final int LIST_HASH = LogicInfoList.id.hashCode();

	/** the info uuid */
	public InfoUUID uuid;
	public ReferenceType refType;
	/** the registered info ID - (-1 = NO SET, -2 = DOESN'T MATTER) */
	public int infoType = -1;

	public Object cachedObject;

	public InfoReference() {}

	public InfoReference(InfoUUID uuid, ReferenceType type, int infoType) {
		this.uuid = uuid;
		this.refType = type;
		this.infoType = infoType;
	}

	public Object getLatestInfo(DisplayGSI gsi) {
		if (cachedObject != null) {
			return cachedObject;
		}
		return null;
	}

	public boolean isList() {
		return infoType == LIST_HASH;
	}

	public void replace(InfoUUID uuid) {
		this.uuid = uuid;
		this.cachedObject = null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof InfoReference) {
			return ((InfoReference)obj).uuid.equals(uuid) && ((InfoReference)obj).refType.equals(refType);
		}
		return false;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		uuid = NBTHelper.instanceNBTSyncable(InfoUUID.class, nbt);
		infoType = nbt.getInteger("infoType");
		refType = ReferenceType.values()[nbt.getInteger("t")];
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		uuid.writeData(nbt, type);
		nbt.setInteger("infoType", infoType);
		nbt.setInteger("t", refType.ordinal());
		return nbt;
	}

}
