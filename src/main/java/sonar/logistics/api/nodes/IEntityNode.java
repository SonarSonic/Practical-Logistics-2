package sonar.logistics.api.nodes;

import java.util.ArrayList;
import java.util.List;

import sonar.core.utils.IRemovable;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.IPriority;

/** implemented by Entity Nodes, provides a list of all the entities they are connected to, normally just one, but can be more */
public interface IEntityNode extends ILogicTile, IPriority {

	public void addEntities(List<NodeConnection> entities);
	
	public static void addEntities(IEntityNode node, ArrayList<NodeConnection> connections){
		if (!(node instanceof IRemovable) || !((IRemovable) node).wasRemoved()) {
			((IEntityNode) node).addEntities(connections);
		}
	}
}
