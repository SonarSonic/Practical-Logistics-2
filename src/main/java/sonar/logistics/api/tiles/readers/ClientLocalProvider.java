package sonar.logistics.api.tiles.readers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

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

/** used when syncing Logic Monitors for display in the Display Screen with the
 * client, since some may not be loaded on client side. */
public class ClientLocalProvider implements INBTSyncable {

	public List<ISyncPart> syncParts = new ArrayList<ISyncPart>();
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncCoords coords = new SyncCoords(1);
	{
		syncParts.addAll(Lists.newArrayList(identity, coords));
	}

	public ClientLocalProvider() {}

	public ClientLocalProvider(ILogicListenable monitor) {
		this.identity.setObject(monitor.getIdentity());
		this.coords.setCoords(monitor.getCoords());
	}

	public ClientLocalProvider(int uuid, BlockCoords coords) {
		this.identity.setObject(uuid);
		this.coords.setCoords(coords);
	}

	public ClientLocalProvider copy() {
		return new ClientLocalProvider(identity.getObject(), coords.getCoords());
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		NBTHelper.readSyncParts(nbt, type, syncParts);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTHelper.writeSyncParts(nbt, type, syncParts, type.isType(SyncType.SAVE));
		return nbt;
	}

	public ILogicListenable getViewable() {
		ILogicListenable viewable = ClientInfoHandler.instance().getIdentityTile(identity.getObject());
		if (viewable != null && viewable instanceof ILogicListenable) {
			ILogicListenable partViewer = (ILogicListenable) viewable;
			viewable = (ILogicListenable) viewable;
		}
		return viewable;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ClientWirelessEmitter) {
			return hashCode() == obj.hashCode() && coords.getCoords().equals(((ClientWirelessEmitter) obj).coords.getCoords());
		}
		return false;
	}

	public int hashCode() {
		return identity.getObject();
	}

}
