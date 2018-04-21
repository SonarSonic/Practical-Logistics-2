package sonar.logistics.api.errors;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.InfoErrorType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.states.ErrorMessage;

@InfoErrorType(id = "uuid_error", modid = PL2Constants.MODID)
public class UUIDError extends AbstractUUIDError implements INBTSyncable, IInfoError {

	public ErrorMessage error;

	public UUIDError() {
		super();
	}

	public UUIDError(InfoUUID uuid, ErrorMessage error) {
		super(uuid);
		this.error = error;
	}

	public UUIDError(List<InfoUUID> uuids, ErrorMessage error) {
		super(uuids);
		this.error = error;
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		error = ErrorMessage.values()[nbt.getInteger("error")];
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setInteger("error", error.ordinal());
		return nbt;
	}

	@Override
	public List<String> getDisplayMessage() {
		return Lists.newArrayList(error.getStateMessage());
	}

	@Override
	public boolean canDisplayInfo(IInfo info) {
		return false;
	}	

}
