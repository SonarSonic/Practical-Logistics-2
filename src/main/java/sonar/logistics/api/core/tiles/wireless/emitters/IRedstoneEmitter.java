package sonar.logistics.api.core.tiles.wireless.emitters;

import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.IWirelessRedstoneTile;

public interface IRedstoneEmitter extends IWirelessEmitter, IWirelessRedstoneTile, IRedstoneConnectable {
	
}
