package sonar.logistics.api.wireless;

import java.util.UUID;

import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.networks.ILogisticsNetwork;

public interface IWirelessReceiver<E extends IWirelessEmitter> extends INetworkTile {
	
	public UUID getOwner();
	
	public void onEmitterSecurityChanged(E emitter, WirelessSecurity oldSetting);
	
	/**only called if isConnectedToEmitter returns true
	 * @param network TODO*/
	public void onEmitterDisconnected(E emitter);

	/**only called if isConnectedToEmitter returns true*/
	public void onEmitterConnected(E emitter);
	
	/**if the emitter is on the list of chosen emitters*/
	public EnumConnected canEmitterAccessReceiver(E emitter);
	
	/**if the receiver can currently connect to the emitter*/
	public default EnumConnected canReceiverAccessEmitter(E emitter, WirelessSecurity emitterSecurity){
		if(emitterSecurity.requiresPermission()){
			return emitter.canPlayerConnect(getOwner());
		}
		return EnumConnected.CONNECTED;	
	}
	
	/**checks if both canEmitterAccessReceiver & canReceiverAccessEmitter are true*/
	public default EnumConnected canAccess(E emitter){
		return EnumConnected.isConnected(canEmitterAccessReceiver(emitter), canReceiverAccessEmitter(emitter, emitter.getSecurity()));
	}

}
