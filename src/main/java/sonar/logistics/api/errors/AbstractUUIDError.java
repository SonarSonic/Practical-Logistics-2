package sonar.logistics.api.errors;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.info.InfoUUID;

public class AbstractUUIDError implements INBTSyncable {

	public List<InfoUUID> uuids;
	
	public AbstractUUIDError(){
		this.uuids = new ArrayList<>();
	}
	
	public AbstractUUIDError(InfoUUID uuid){
		this.uuids = Lists.newArrayList(uuid);		
	}
	
	public AbstractUUIDError(List<InfoUUID> uuids){
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