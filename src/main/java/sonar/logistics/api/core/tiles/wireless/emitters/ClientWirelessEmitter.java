package sonar.logistics.api.core.tiles.wireless.emitters;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.ISyncPart;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncTagType;

import java.util.ArrayList;
import java.util.List;

/** used when syncing Data Emitters for display in the Data Receiver with the client, since some may not be loaded on client side. */
public class ClientWirelessEmitter implements INBTSyncable {

	public List<ISyncPart> syncParts = new ArrayList<>();
	public SyncTagType.INT identity = new SyncTagType.INT(0);
	public SyncCoords coords = new SyncCoords(1);
	public SyncTagType.STRING name = new SyncTagType.STRING(2);
	{
		syncParts.addAll(Lists.newArrayList(identity, coords, name));
	}

	public ClientWirelessEmitter() {
	}

	public ClientWirelessEmitter(IWirelessEmitter emitter) {
		this.identity.setObject(emitter.getIdentity());
		this.coords.setCoords(emitter.getCoords());
		this.name.setObject(emitter.getEmitterName());
	}

	public ClientWirelessEmitter(int uuid, BlockCoords coords, String name) {
		this.identity.setObject(uuid);
		this.coords.setCoords(coords);
		this.name.setObject(name);
	}
	
	public int getIdentity(){
		return identity.getObject();
	}

	public ClientWirelessEmitter copy() {
		return new ClientWirelessEmitter(identity.getObject(), coords.getCoords(), name.getObject());
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
		if (obj instanceof ClientWirelessEmitter) {
			return hashCode() == obj.hashCode() && coords.getCoords().equals(((ClientWirelessEmitter) obj).coords.getCoords());
		}
		return false;
	}

	public int hashCode() {
		return identity.getObject();
	}

}
