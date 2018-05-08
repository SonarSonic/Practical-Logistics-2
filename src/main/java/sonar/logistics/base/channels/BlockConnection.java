package sonar.logistics.base.channels;

import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;

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
		if (obj instanceof BlockConnection) {
			return ((BlockConnection) obj).coords.equals(coords);
		}
		return false;
	}

	@Override
	public MonitoredBlockCoords getChannel() {
		return new MonitoredBlockCoords(coords, LogisticsHelper.getCoordItem(coords, coords.getWorld()));
	}

	@Override
	public NodeConnectionType getType() {
		return NodeConnectionType.TILE;
	}
}
