package sonar.logistics.core.tiles.displays.info.types;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.INameableInfo;
import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;

@ASMInfo(id = InfoError.id, modid = PL2Constants.MODID)
public class InfoError implements IInfo<InfoError>, INameableInfo<InfoError> {

	public static final InfoError noData = new InfoError("NO DATA");
	public static final InfoError noMonitor = new InfoError("NO MONITOR");
	public static final InfoError noItem = new InfoError("NO ITEMSTACK");
	public static final InfoError incompleteDisplay = new InfoError("INCOMPLETE");

	public static final String id = "error";
	public String error;

	public InfoError() {}

	public InfoError(String error) {
		this.error = FontHelper.translate(error == null ? "" : error);
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public boolean isIdenticalInfo(InfoError info) {
		return info.error.equals(error);
	}

	@Override
	public boolean isMatchingInfo(InfoError info) {
		return true;
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof InfoError;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public InfoError copy() {
		return new InfoError(error);
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		error = nbt.getString("error");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setString("error", error);
		return nbt;
	}

	@Override
	public void identifyChanges(InfoError newInfo) {}

	@Override
	public LogicPath getPath() {
		return null;
	}

	@Override
	public InfoError setPath(LogicPath path) {
		return this;
	}
	
	@Override
	public void onInfoStored() {}

	@Override
	public String getClientIdentifier() {
		return "Info Error";
	}

	@Override
	public String getClientObject() {
		return error;
	}

	@Override
	public String getClientType() {
		return "error";
	}
}
