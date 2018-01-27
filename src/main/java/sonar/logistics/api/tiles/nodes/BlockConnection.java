package sonar.logistics.api.tiles.nodes;

import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.info.types.MonitoredBlockCoords;

public class BlockConnection extends NodeConnection<MonitoredBlockCoords> {

	public BlockCoords coords;
	public EnumFacing face;

	public BlockConnection(INetworkTile source, BlockCoords coords, EnumFacing face) {
		super(source);
		this.coords = coords;
		this.face = face;
	}

	public int hashCode() {
		return coords.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof BlockConnection) {
			return ((BlockConnection) obj).coords.equals(coords);
		}
		return false;
	}

	@Override
	public MonitoredBlockCoords getChannel() {
		return new MonitoredBlockCoords(coords, LogisticsHelper.getCoordItem(coords));
	}

	@Override
	public NodeConnectionType getType() {
		return NodeConnectionType.TILE;
	}
}
