package sonar.logistics.api.wireless;

public interface IRedstoneReceiver extends IWirelessReceiver<IRedstoneEmitter>, IWirelessRedstoneTile {

	public void updatePower();
	
	public void onEmitterPowerChanged(IRedstoneEmitter emitter);
}
