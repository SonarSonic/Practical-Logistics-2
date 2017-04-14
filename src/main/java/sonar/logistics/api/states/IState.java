package sonar.logistics.api.states;

public interface IState {

	/**can the respective tile still be opened in this state*/
	public boolean canOpenTile();
	
	/**for the client, this is a short message explaining the problem and how to fix it.*/
	public String getStateMessage();
	
	/**the registered state ID*/
	public int getStateID();		
	
}
