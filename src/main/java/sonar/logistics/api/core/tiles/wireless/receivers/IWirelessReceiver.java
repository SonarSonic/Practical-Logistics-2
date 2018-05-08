package sonar.logistics.api.core.tiles.wireless.receivers;

import sonar.logistics.api.core.tiles.wireless.EnumWirelessConnectionState;
import sonar.logistics.api.core.tiles.wireless.EnumWirelessSecurity;
import sonar.logistics.api.core.tiles.wireless.emitters.IWirelessEmitter;
import sonar.logistics.base.tiles.INetworkTile;

import java.util.UUID;

public interface IWirelessReceiver<E extends IWirelessEmitter> extends INetworkTile {
	
	UUID getOwner();
	
	void onEmitterSecurityChanged(E emitter, EnumWirelessSecurity oldSetting);
	
	/**only called if isConnectedToEmitter returns true*/
    void onEmitterDisconnected(E emitter);

	/**only called if isConnectedToEmitter returns true*/
    void onEmitterConnected(E emitter);
	
	/**if the emitters is on the list of chosen emitters*/
    EnumWirelessConnectionState canEmitterAccessReceiver(E emitter);
	
	/**if the receivers can currently connect to the emitters*/
	default EnumWirelessConnectionState canReceiverAccessEmitter(E emitter, EnumWirelessSecurity emitterSecurity){
		if(emitterSecurity.requiresPermission()){
			return emitter.canPlayerConnect(getOwner());
		}
		return EnumWirelessConnectionState.CONNECTED;
	}
	
	/**checks if both canEmitterAccessReceiver & canReceiverAccessEmitter are true*/
	default EnumWirelessConnectionState canAccess(E emitter){
		return EnumWirelessConnectionState.isConnected(canEmitterAccessReceiver(emitter), canReceiverAccessEmitter(emitter, emitter.getSecurity()));
	}

}
