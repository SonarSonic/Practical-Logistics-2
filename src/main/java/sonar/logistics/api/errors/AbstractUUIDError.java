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
		List<InfoUUID> newUUIDs = new ArrayList<>();
		NBTTagList tagList = nbt.getTagList("uuids", NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			InfoUUID loaded = new InfoUUID();
			loaded.readData(tagList.getCompoundTagAt(i), type);
			newUUIDs.add(loaded);
		}
		uuids = newUUIDs;		
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagList tagList = new NBTTagList();
		uuids.forEach(obj -> tagList.appendTag(obj.writeData(new NBTTagCompound(), SyncType.SAVE)));
		nbt.setTag("uuids", tagList);
		return nbt;
	}	

	public List<InfoUUID> getAffectedUUIDs() {
		return uuids;
	}
	
	public boolean isValid(){
		return !uuids.isEmpty();
	}
}