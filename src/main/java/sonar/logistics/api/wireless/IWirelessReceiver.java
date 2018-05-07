package sonar.logistics.api.wireless;

import sonar.logistics.api.tiles.INetworkTile;

import java.util.UUID;

public interface IWirelessReceiver<E extends IWirelessEmitter> extends INetworkTile {
	
	UUID getOwner();
	
	void onEmitterSecurityChanged(E emitter, WirelessSecurity oldSetting);
	
	/**only called if isConnectedToEmitter returns true*/
    void onEmitterDisconnected(E emitter);

	/**only called if isConnectedToEmitter returns true*/
    void onEmitterConnected(E emitter);
	
	/**if the emitter is on the list of chosen emitters*/
    EnumConnected canEmitterAccessReceiver(E emitter);
	
	/**if the receiver can currently connect to the emitter*/
	default EnumConnected canReceiverAccessEmitter(E emitter, WirelessSecurity emitterSecurity){
		if(emitterSecurity.requiresPermission()){
			return emitter.canPlayerConnect(getOwner());
		}
		return EnumConnected.CONNECTED;	
	}
	
	/**checks if both canEmitterAccessReceiver & canReceiverAccessEmitter are true*/
	default EnumConnected canAccess(E emitter){
		return EnumConnected.isConnected(canEmitterAccessReceiver(emitter), canReceiverAccessEmitter(emitter, emitter.getSecurity()));
	}

}
