package sonar.logistics.api.cabling;

import sonar.logistics.networking.cabling.IRedstoneNetwork;

public interface IRedstoneConnectable extends ICableConnectable, IRedstoneListener {
	
	int getIdentity();
	
	IRedstoneNetwork getRedstoneNetwork();
	
	default void onCableChanged(int power){
		
	}
}
