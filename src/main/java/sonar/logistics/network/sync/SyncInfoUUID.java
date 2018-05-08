package sonar.logistics.network.sync;

import sonar.core.network.sync.SyncNBTAbstract;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;

public class SyncInfoUUID extends SyncNBTAbstract<InfoUUID> {

	public SyncInfoUUID(int id) {
		super(InfoUUID.class, id);
	}

	public boolean isValid(InfoUUID obj) {
		return InfoUUID.valid(obj);
	}

}
