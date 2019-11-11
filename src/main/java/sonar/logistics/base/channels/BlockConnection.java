package sonar.logistics.base.channels;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import sonar.core.api.utils.BlockCoords;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.LogisticsHelper;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;

import javax.annotation.Nullable;

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

	@Nullable
	@Override
	public IItemHandler getItemHandler() {
		/*
		TileEntity tile = coords.getTileEntity();
		if (tile != null) {
			for (ITileInventoryProvider provider : MasterInfoRegistry.INSTANCE.inventoryProviders) {
				IItemHandler handler = provider.getHandler(tile, face);
				if (handler != null) {
					return handler;
				}
			}
		}
		*/
		return null;
	}

	@Override
	public NodeConnectionType getType() {
		return NodeConnectionType.TILE;
	}
}
