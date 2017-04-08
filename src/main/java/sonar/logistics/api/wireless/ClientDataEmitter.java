package sonar.logistics.api.wireless;

import java.util.ArrayList;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncUUID;
import sonar.core.utils.IUUIDIdentity;

/** used when syncing Data Emitters for display in the Data Receiver with the client, since some may not be loaded on client side. */
public class ClientDataEmitter implements INBTSyncable {

	public ArrayList<ISyncPart> syncParts = new ArrayList<ISyncPart>();
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncCoords coords = new SyncCoords(1);
	public SyncTagType.STRING name = new SyncTagType.STRING(2);
	{
		syncParts.addAll(Lists.newArrayList(identity, coords, name));
	}

	public ClientDataEmitter() {
	}

	public ClientDataEmitter(IDataEmitter emitter) {
		this.identity.setObject(emitter.getIdentity());
		this.coords.setCoords(emitter.getCoords());
		this.name.setObject(emitter.getEmitterName());
	}

	public ClientDataEmitter(int uuid, BlockCoords coords, String name) {
		this.identity.setObject(uuid);
		this.coords.setCoords(coords);
		this.name.setObject(name);
	}
	
	public int getIdentity(){
		return identity.getObject();
	}

	public ClientDataEmitter copy() {
		return new ClientDataEmitter(identity.getObject(), coords.getCoords(), name.getObject());
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
