package sonar.logistics.api.core.tiles.connections.data.network;

import net.minecraftforge.items.IItemHandler;

public interface INetworkItemHandler extends IItemHandler {

    IItemHandler initiliseTransfer();
}
