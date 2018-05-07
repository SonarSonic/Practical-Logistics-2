package sonar.logistics.api.cabling;

import net.minecraft.util.EnumFacing;

/**used by tiles which render cables, this includes Cables themselves*/
public interface ICableRenderer {

	/**used by the client to check if the cable can connect, if it can it will render the connection*/
    ConnectableType canRenderConnection(EnumFacing dir);
}
