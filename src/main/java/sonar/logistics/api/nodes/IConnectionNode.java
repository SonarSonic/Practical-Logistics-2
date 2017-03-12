package sonar.logistics.api.nodes;

import java.util.ArrayList;

import sonar.core.utils.IRemovable;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.IPriority;

/** implemented by Nodes, provides a list of all the blocks they are connected to, normally just one, but can be more */
public interface IConnectionNode extends ILogicTile, IPriority {

	/** adds any available connections to the current Map
	 * @param connections the current list of Entries */
	public void addConnections(ArrayList<NodeConnection> connections);
	
	public static void addConnections(IConnectionNode node, ArrayList<NodeConnection> channels){
		if (!(node instanceof IRemovable) || !((IRemovable) node).wasRemoved()) {
			((IConnectionNode) node).addConnections(channels);
		}
	}
}
