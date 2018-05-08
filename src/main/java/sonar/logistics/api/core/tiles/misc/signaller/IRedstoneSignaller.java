package sonar.logistics.api.core.tiles.misc.signaller;

import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.utils.IWorldPosition;
import sonar.logistics.core.tiles.misc.signaller.RedstoneSignallerStatement;

public interface IRedstoneSignaller extends IWorldPosition {

	SyncNBTAbstractList<RedstoneSignallerStatement> getStatements();
	
	SyncEnum<SignallerModes> emitterMode();
}
