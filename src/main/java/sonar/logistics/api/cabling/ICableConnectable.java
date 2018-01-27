package sonar.logistics.api.cabling;

import net.minecraft.util.EnumFacing;

public interface ICableConnectable {

	/** can the Tile connect to cables on the given direction */
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal);

	/** for internal connections */
	public CableRenderType getCableRenderSize(EnumFacing dir);
}
