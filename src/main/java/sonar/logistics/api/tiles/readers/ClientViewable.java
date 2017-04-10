package sonar.logistics.api.tiles.readers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.multipart.IMultipart;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.helpers.CableHelper;

/** used when syncing Logic Monitors for display in the Display Screen with the client, since some may not be loaded on client side. */
public class ClientViewable implements INBTSyncable {

	public List<ISyncPart> syncParts = new ArrayList<ISyncPart>();
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncCoords coords = new SyncCoords(1);
	{
		syncParts.addAll(Lists.newArrayList(identity, coords));
	}

	public ClientViewable() {
	}

	public ClientViewable(ILogicListenable monitor) {
		this.identity.setObject(monitor.getIdentity());
		this.coords.setCoords(monitor.getCoords());
	}

	public ClientViewable(int uuid, BlockCoords coords) {
		this.identity.setObject(uuid);
		this.coords.setCoords(coords);
	}

	public ClientViewable copy() {
		return new ClientViewable(identity.getObject(), coords.getCoords());
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
		ILogicListenable viewable = CableHelper.getMonitorFromIdentity(identity.getObject().hashCode(), true);
		if (viewable == null) {
			IMultipart part = SonarMultipartHelper.getPartFromHash(identity.getObject().hashCode(), coords.getCoords().getWorld(), coords.getCoords().getBlockPos());
			if (part != null && part instanceof ILogicListenable) {
				ILogicListenable partViewer = (ILogicListenable) part;
				viewable = (ILogicListenable) part;
			}
		}
		return viewable;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ClientDataEmitter) {
			return hashCode() == obj.hashCode() && coords.getCoords().equals(((ClientDataEmitter) obj).coords.getCoords());
		}
		return false;
	}

	public int hashCode() {
		return identity.getObject();
	}

}
