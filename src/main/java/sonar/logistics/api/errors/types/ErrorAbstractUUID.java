package sonar.logistics.api.errors.types;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.info.InfoUUID;

import java.util.ArrayList;
import java.util.List;

public class ErrorAbstractUUID implements INBTSyncable {

	public List<InfoUUID> uuids;
	
	public ErrorAbstractUUID(){
		this.uuids = new ArrayList<>();
	}
	
	public ErrorAbstractUUID(InfoUUID uuid){
		this.uuids = Lists.newArrayList(uuid);		
	}
	
	public ErrorAbstractUUID(List<InfoUUID> uuids){
		this.uuids = uuids;		
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		uuids = InfoUUID.readInfoList(nbt, "uuids");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt = InfoUUID.writeInfoList(nbt, uuids, "uuids");
		return nbt;
	}	

	public List<InfoUUID> getAffectedUUIDs() {
		return uuids;
	}
	
	public boolean isValid(){
		return !uuids.isEmpty();
	}
}