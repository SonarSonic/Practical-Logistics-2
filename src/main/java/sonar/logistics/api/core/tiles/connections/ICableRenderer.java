package sonar.logistics.api.core.tiles.connections;

import net.minecraft.util.EnumFacing;

/**used by base which render connections, this includes Cables themselves*/
public interface ICableRenderer {

	/**used by the client to check if the cable can connect, if it can it will render the connection*/
    EnumCableConnectionType canRenderConnection(EnumFacing dir);
}
