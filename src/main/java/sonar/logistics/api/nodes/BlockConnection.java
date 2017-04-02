package sonar.logistics.api.nodes;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.connections.monitoring.MonitoredBlockCoords;

public class BlockConnection extends NodeConnection<MonitoredBlockCoords> {

	public BlockCoords coords;
	public EnumFacing face;

	public BlockConnection(ILogicTile source, BlockCoords coords, EnumFacing face) {
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
}
