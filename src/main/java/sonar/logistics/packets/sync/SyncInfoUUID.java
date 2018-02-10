package sonar.logistics.packets.sync;

import sonar.core.network.sync.SyncNBTAbstract;
import sonar.logistics.api.info.InfoUUID;

public class SyncInfoUUID extends SyncNBTAbstract<InfoUUID> {

	public SyncInfoUUID(int id) {
		super(InfoUUID.class, id);
	}

	public boolean isValid(InfoUUID obj) {
		return InfoUUID.valid(obj);
	}

}
