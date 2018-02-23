package sonar.logistics.api.displays.elements;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;

public abstract class AbstractInfoElement<T extends IInfo> extends AbstractDisplayElement {

	public InfoUUID uuid;
	public IInfo info;

	public AbstractInfoElement() {}

	public AbstractInfoElement(InfoUUID uuid) {
		this.uuid = uuid;
	}

	public final void render() {
		info = getGSI().getCachedInfo(uuid);
		if (info != null && isType(info)) {
			render((T) info);
		}else{
			renderNoData();
		}
	}

	public void renderNoData() {
		FontHelper.text("WAITING FOR SERVER", 0, 0, -1);
	}

	public abstract boolean isType(IInfo info);

	public abstract void render(T info);

	@Override
	public List<InfoUUID> getInfoReferences() {
		return Lists.newArrayList(uuid);
	}

	@Override
	public String getRepresentiveString() {
		return uuid.toString();
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		(uuid = new InfoUUID()).readData(nbt, type);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		uuid.writeData(nbt, type);
		return nbt;
	}

}
