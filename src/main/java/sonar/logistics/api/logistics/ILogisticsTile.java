package sonar.logistics.api.logistics;

import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.utils.IUUIDIdentity;
import sonar.core.utils.IWorldPosition;

public interface ILogisticsTile extends IWorldPosition {

	public SyncNBTAbstractList<EmitterStatement> getStatements();
	
	public SyncEnum<SignallerModes> emitterMode();
}
