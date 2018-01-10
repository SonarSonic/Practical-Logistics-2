package sonar.logistics.info.types;

import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.utils.MonitoredValue;
import sonar.logistics.connections.handlers.ItemNetworkHandler;

public abstract class NewMonitoredList<T> extends BaseInfo<NewMonitoredList> {

	public int hashCode;

	public NewMonitoredList(int hashCode) {
		this.hashCode = hashCode;
	}

	public abstract boolean equal(T key1, T key2);

	public abstract T copy(T copy);

	public void added(T object, long added) {}

	public void removed(T object, long removed) {}
	
	public abstract NBTTagCompound writeToNBT(NBTTagCompound tag, T object, MonitoredValue stored);
	
	public abstract NBTTagCompound readFromNBT(NBTTagCompound tag, T object, MonitoredValue stored);

	@Override
	public boolean isIdenticalInfo(NewMonitoredList info) {
		return true;
	}

	@Override
	public boolean isMatchingInfo(NewMonitoredList info) {
		return info.hashCode() == hashCode();
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof NewMonitoredList;
	}

	@Override
	public INetworkHandler getHandler() {
		return ItemNetworkHandler.INSTANCE;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public NewMonitoredList copy() {
		return null; // don't copy you loser
	}

	public int hashCode() {
		return hashCode;
	}

}