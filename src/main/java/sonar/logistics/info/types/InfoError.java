package sonar.logistics.info.types;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.LogicInfoType;
import sonar.logistics.api.displays.DisplayConstants;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.connections.monitoring.LogicMonitorHandler;
import sonar.logistics.helpers.InfoRenderer;

@LogicInfoType(id = InfoError.id, modid = PL2Constants.MODID)
public class InfoError implements IMonitorInfo<InfoError> {

	public static final InfoError noData = new InfoError("NO DATA");
	public static final InfoError noMonitor = new InfoError("NO MONITOR");
	public static final InfoError noItem = new InfoError("NO ITEMSTACK");
	
	public static final String id = "error";
	public String error;

	public InfoError(){}
	
	public InfoError(String error) {
		this.error = FontHelper.translate(error);
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
	public boolean isMatchingType(IMonitorInfo info) {
		return info instanceof InfoError;
	}

	@Override
	public boolean isHeader() {
		return false;
	}

	@Override
	public LogicMonitorHandler<InfoError> getHandler() {
		return null;
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
	public void renderInfo(InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		InfoRenderer.renderNormalInfo(container.display.getDisplayType(), width, height, scale, DisplayConstants.formatText(error, displayInfo));
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
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

}
