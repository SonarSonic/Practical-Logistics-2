package sonar.logistics.api.operator;

/**all the different modes of an IOperatorTool*/
public enum OperatorMode {

	DEFAULT,/**performs the action specied with IOperatorTile*/	
	ROTATE,/**rotates the block*/	
	INFO,/**tiles all the info provided by IOperatorProvider*/
	CHANNELS,/***/
	ANALYSE /***/

}
