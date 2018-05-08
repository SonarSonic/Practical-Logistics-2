package sonar.logistics.api.core.tiles.connections.redstone;

import sonar.logistics.api.core.tiles.connections.ICableConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;

public interface IRedstoneConnectable extends ICableConnectable, IRedstoneListener {
	
	int getIdentity();
	
	IRedstoneNetwork getRedstoneNetwork();
	
	default void onCableChanged(int power){
		
	}
}
