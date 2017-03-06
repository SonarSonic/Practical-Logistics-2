package sonar.logistics.api.nodes;

import net.minecraft.util.EnumFacing;
import sonar.core.api.utils.BlockCoords;

public class BlockConnection extends NodeConnection {

	public BlockCoords coords;
	public EnumFacing face;
	
	public BlockConnection(IConnectionNode source, BlockCoords coords, EnumFacing face) {
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
}
