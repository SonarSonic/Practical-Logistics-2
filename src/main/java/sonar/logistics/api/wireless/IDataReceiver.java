package sonar.logistics.api.wireless;

import java.util.List;
import java.util.UUID;

import sonar.logistics.api.tiles.INetworkTile;

/** implemented on the Data Receiver */
public interface IDataReceiver extends INetworkTile {

	/** gets the network ID of all currently connected networks */
	public List<Integer> getConnectedNetworks();

	/** rechecks connected Data Emitters to ensure the connected network IDs are correct. Typically triggered by an alert {@link RefreshType} */
	public void refreshConnectedNetworks();
	
	public UUID getOwner();
	
	public void onEmitterSecurityChanged(IDataEmitter emitter, DataEmitterSecurity oldSetting);
	
	/**only called if isConnectedToEmitter returns true*/
	public void onEmitterDisconnected(IDataEmitter emitter);

	/**only called if isConnectedToEmitter returns true*/
	public void onEmitterConnected(IDataEmitter emitter);
	
	/**if the emitter is on the list of chosen emitters*/
	public boolean canEmitterAccessReceiver(IDataEmitter emitter);
	
	/**if the receiver can currently connect to the emitter*/
	public default boolean canReceiverAccessEmitter(IDataEmitter emitter, DataEmitterSecurity emitterSecurity){
		if(emitterSecurity.requiresPermission()){
			return emitter.canPlayerConnect(getOwner());
		}
		return true;	
	}
	
	/**checks if both canEmitterAccessReceiver & canReceiverAccessEmitter are true*/
	public default boolean canAccess(IDataEmitter emitter){
		return canEmitterAccessReceiver(emitter) && canReceiverAccessEmitter(emitter, emitter.getSecurity());
	}

}
