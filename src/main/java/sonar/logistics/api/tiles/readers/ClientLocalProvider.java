package sonar.logistics.api.tiles.readers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wireless.ClientWirelessEmitter;
import sonar.logistics.networking.ClientInfoHandler;
import sonar.logistics.networking.sorters.SortingHelper;

/** used when syncing Logic Monitors for display in the Display Screen with the client, since some may not be loaded on client side. */
public class ClientLocalProvider implements INBTSyncable {

	public List<ISyncPart> syncParts = new ArrayList<>();
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncCoords coords = new SyncCoords(1);
	public ItemStack stack;
	public ILogicListSorter sorter;
	{
		syncParts.addAll(Lists.newArrayList(identity, coords));
	}

	public ClientLocalProvider() {}

	public ClientLocalProvider(ILogicListenable monitor, ILogicListSorter sorter, ItemStack stack) {
		this.identity.setObject(monitor.getIdentity());
		this.coords.setCoords(monitor.getCoords());
		this.sorter = sorter;
		this.stack = stack;
	}

	public ClientLocalProvider(int uuid, BlockCoords coords, ILogicListSorter sorter, ItemStack stack) {
		this.identity.setObject(uuid);
		this.coords.setCoords(coords);
		this.sorter = sorter;
		this.stack = stack;
	}

	public ClientLocalProvider copy() {
		return new ClientLocalProvider(identity.getObject(), coords.getCoords(), SortingHelper.copySorter(sorter), stack.copy());
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		NBTHelper.readSyncParts(nbt, type, syncParts);
		if (nbt.hasKey("sorter")) {
			sorter = SortingHelper.loadListSorter(nbt.getCompoundTag("sorter"));
		}
		stack = new ItemStack(nbt.getCompoundTag("stack"));
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTHelper.writeSyncParts(nbt, type, syncParts, type.isType(SyncType.SAVE));
		if (sorter != null) {
			nbt.setTag("sorter", SortingHelper.saveListSorter(new NBTTagCompound(), sorter, SyncType.SAVE));
		}
		nbt.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
		return nbt;
	}

	public ILogicListenable getViewable() {
		return ClientInfoHandler.instance().getIdentityTile(identity.getObject());
	}

	public boolean equals(Object obj) {
		if (obj instanceof ClientWirelessEmitter) {
			return hashCode() == obj.hashCode() && coords.getCoords().equals(((ClientWirelessEmitter) obj).coords.getCoords());
		}
		return false;
	}

	public int hashCode() {
		return identity.getObject();
	}

}
