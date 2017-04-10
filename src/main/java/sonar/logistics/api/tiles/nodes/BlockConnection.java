package sonar.logistics.api.tiles.nodes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.utils.NodeConnectionType;
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
		TileEntity tile = coords.getTileEntity();
		return new MonitoredBlockCoords(coords, tile != null && tile.getDisplayName() != null ? tile.getDisplayName().getFormattedText() : coords.getBlock().getLocalizedName());
	}

	@Override
	public NodeConnectionType getType() {
		return NodeConnectionType.TILE;
	}
}
