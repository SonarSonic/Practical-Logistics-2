package sonar.logistics.base.guidance.errors;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfoError;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;

import java.util.List;

@ASMInfoError(id = "uuid_error", modid = PL2Constants.MODID)
public class ErrorUUID extends ErrorAbstractUUID implements INBTSyncable, IInfoError {

	public ErrorMessage error;

	public ErrorUUID() {
		super();
	}

	public ErrorUUID(InfoUUID uuid, ErrorMessage error) {
		super(uuid);
		this.error = error;
	}

	public ErrorUUID(List<InfoUUID> uuids, ErrorMessage error) {
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
