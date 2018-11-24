package sonar.logistics.core.tiles.nodes.transfer.handling;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;

import javax.annotation.Nonnull;
import java.util.List;

//TODO
public class NetworkItemHandler implements IItemHandler {

    public ILogisticsNetwork net;
    public List<IItemHandler> subHandlers; ///iterate through this list, getSlots() == all of them added up etc.

    public NetworkItemHandler(ILogisticsNetwork net){
        this.net = net;
    }

    @Override
    public int getSlots() {
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return null;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return null;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }
}
