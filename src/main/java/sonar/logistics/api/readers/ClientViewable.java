package sonar.logistics.api.readers;

import java.util.ArrayList;
import java.util.UUID;

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
import sonar.core.network.sync.SyncUUID;
import sonar.core.utils.IUUIDIdentity;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.api.wireless.ClientDataEmitter;
import sonar.logistics.helpers.CableHelper;

/** used when syncing Logic Monitors for display in the Display Screen with the client, since some may not be loaded on client side. */
public class ClientViewable implements IUUIDIdentity, INBTSyncable {

	public ArrayList<ISyncPart> syncParts = new ArrayList<ISyncPart>();
	public SyncUUID identity = new SyncUUID(0);
	public SyncCoords coords = new SyncCoords(1);
	{
		syncParts.addAll(Lists.newArrayList(identity, coords));
	}

	public ClientViewable() {
	}

	public ClientViewable(ILogicViewable monitor) {
		this.identity.setObject(monitor.getIdentity());
		this.coords.setCoords(monitor.getCoords());
	}

	public ClientViewable(UUID uuid, BlockCoords coords) {
		this.identity.setObject(uuid);
		this.coords.setCoords(coords);
	}

	public ClientViewable copy() {
		return new ClientViewable(identity.getUUID(), coords.getCoords());
	}

	@Override
	public UUID getIdentity() {
		return identity.getUUID();
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

	public ILogicViewable getViewable() {
		ILogicViewable viewable = CableHelper.getMonitorFromHashCode(identity.getUUID().hashCode(), true);
		if (viewable == null) {
			IMultipart part = SonarMultipartHelper.getPartFromHash(identity.getUUID().hashCode(), coords.getCoords().getWorld(), coords.getCoords().getBlockPos());
			if (part != null && part instanceof ILogicViewable) {
				ILogicViewable partViewer = (ILogicViewable) part;
				viewable = (ILogicViewable) part;			
			}
		}
		return viewable;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof ClientDataEmitter) {
			return getIdentity().equals(((IUUIDIdentity) obj).getIdentity()) && coords.getCoords().equals(((ClientDataEmitter) obj).coords.getCoords());
		}
		return false;
	}

	public int hashCode() {
		return getIdentity().hashCode();
	}

}
