package sonar.logistics.api.errors;

public interface IState {

	/**can the respective tile still be opened in this state*/
    boolean canOpenTile();
	
	/**for the client, this is a short message explaining the problem and how to fix it.*/
    String getStateMessage();
	
	/**the registered state ID*/
    int getStateID();
	
}
