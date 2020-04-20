package sonar.logistics.base.data.generators.items;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import sonar.core.handlers.inventories.handling.ItemTransferHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMTileInventoryProvider;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;

@ASMTileInventoryProvider(modid = PL2Constants.MODID, priority = 0)
public class TileInventoryForge implements ITileInventoryProvider {

    @Override
    public IItemHandler getHandler(TileEntity tile, EnumFacing face) {
        return tile.getCapability(ItemTransferHelper.ITEM_HANDLER_CAPABILITY, face);
    }

    @Override
    public void getItemList(ItemChangeableList list, IItemHandler handler, TileEntity tile, EnumFacing face) {
        for(int i = 0; i < handler.getSlots(); i++){
            ItemStack stack = handler.getStackInSlot(i);
            if(!stack.isEmpty()) {
                list.add(stack, stack.getCount(), handler.getSlotLimit(i));
            }
        }
    }
}
