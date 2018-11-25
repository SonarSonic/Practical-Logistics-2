package sonar.logistics.core.tiles.nodes.transfer.handling;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.connections.data.network.INetworkItemHandler;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.utils.CacheType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkItemHandler implements INetworkItemHandler {

    public ILogisticsNetwork network;
    public Map<NodeConnection, IItemHandler> handlers;
    public int slots = 0;

    public NetworkItemHandler(ILogisticsNetwork network){
        this.network = network;
    }

    /**should be called before interacting with the Network Handler
     * perhaps in future this should be auto updated somehow*/
    public IItemHandler initiliseTransfer(){
        slots = 0;
        handlers = new HashMap<>();
        List<NodeConnection> connections = network.getConnections(CacheType.ALL);
        for(NodeConnection connection : connections) {
            IItemHandler handler = connection.getItemHandler();
            if(handler != null){
                slots += handler.getSlots();
                handlers.put(connection, handler);
            }
        }
        return this;
    }

    @Override
    public int getSlots() {
        return slots;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        int current = 0;
        for(Map.Entry<NodeConnection, IItemHandler> entry : handlers.entrySet()){
            int size = entry.getValue().getSlots();
            if(current + size > slot){
                return entry.getValue().getStackInSlot(slot - current);
            }else{
                current += size;
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        int current = 0;
        for(Map.Entry<NodeConnection, IItemHandler> entry : handlers.entrySet()){
            int size = entry.getValue().getSlots();
            if(current + size > slot){
                if(entry.getKey().canTransferItem(entry.getKey(), stack, NodeTransferMode.ADD)) {
                    return entry.getValue().insertItem(slot - current, stack, simulate);
                }
                return stack;
            }else{
                current += size;
            }
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        int current = 0;
        for(Map.Entry<NodeConnection, IItemHandler> entry : handlers.entrySet()){
            int size = entry.getValue().getSlots();
            if(current + size > slot){
                if(entry.getKey().canTransferItem(entry.getKey(), entry.getValue().getStackInSlot(slot - current), NodeTransferMode.REMOVE)) {
                    return entry.getValue().extractItem(slot - current, amount, simulate);
                }
                return ItemStack.EMPTY;
            }else{
                current += size;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        int current = 0;
        for(Map.Entry<NodeConnection, IItemHandler> entry : handlers.entrySet()){
            int size = entry.getValue().getSlots();
            if(current + size > slot){
                return entry.getValue().getSlotLimit(slot - current);
            }else{
                current += size;
            }
        }
        return 0;
    }

}
