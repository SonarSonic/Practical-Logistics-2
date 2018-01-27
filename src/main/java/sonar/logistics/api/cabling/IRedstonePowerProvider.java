package sonar.logistics.api.cabling;

public interface IRedstonePowerProvider extends IRedstoneConnectable {
	
	public int getCurrentPower();

}
