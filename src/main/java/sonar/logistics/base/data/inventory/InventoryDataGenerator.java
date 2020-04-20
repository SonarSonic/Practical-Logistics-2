package sonar.logistics.base.data.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.items.IItemHandler;
import sonar.core.handlers.inventories.handling.ItemTransferHelper;
import sonar.logistics.base.data.api.IDataGenerator;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.sources.IDataSource;
import sonar.logistics.base.data.sources.SourceCoord4D;

public class InventoryDataGenerator implements IDataGenerator<SourceCoord4D, InventoryData> {

    @Override
    public boolean canGenerateForSource(IDataSource source) {
        return source instanceof SourceCoord4D;
    }

    @Override
    public boolean updateData(DataHolder holder, InventoryData data, SourceCoord4D source) {
        ///TODO SHOULD WE CHECK THE SOURCE IS LOADED?
        data.onGenerationStart();

        World world = DimensionManager.getWorld(source.getDimension());
        BlockPos pos = source.getPos();
        if(world.isBlockLoaded(pos)){
            TileEntity tile = world.getTileEntity(pos);
            if(tile != null){
                IItemHandler handler = tile.getCapability(ItemTransferHelper.ITEM_HANDLER_CAPABILITY, source.facing);
                if(handler != null){
                    for(int i = 0; i < handler.getSlots(); i++){
                        ItemStack stack = handler.getStackInSlot(i);
                        if(!stack.isEmpty()) {
                            data.addStack(stack, stack.getCount());
                            data.addStorage(stack.getCount(), handler.getSlotLimit(i));
                        }
                    }
                }
            }
        }
        return true; //TODO check there has been a change!
    }

}
