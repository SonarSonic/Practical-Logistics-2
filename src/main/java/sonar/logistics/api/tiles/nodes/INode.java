package sonar.logistics.api.tiles.nodes;

import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.IPriority;

import java.util.List;

/** implemented by Nodes, provides a list of all the blocks they are connected to, normally just one, but can be more */
public interface INode extends INetworkTile, IPriority {

	/** adds any available connections to the current Map
	 * @param connections the current list of Entries */
    void addConnections(List<NodeConnection> connections);
}
