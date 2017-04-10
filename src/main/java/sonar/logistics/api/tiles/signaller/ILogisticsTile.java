package sonar.logistics.api.tiles.signaller;

import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.utils.IWorldPosition;

public interface ILogisticsTile extends IWorldPosition {

	public SyncNBTAbstractList<EmitterStatement> getStatements();
	
	public SyncEnum<SignallerModes> emitterMode();
}
