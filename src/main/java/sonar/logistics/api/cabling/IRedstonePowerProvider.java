package sonar.logistics.api.cabling;

public interface IRedstonePowerProvider extends IRedstoneConnectable {
	
	int getCurrentPower();

}
