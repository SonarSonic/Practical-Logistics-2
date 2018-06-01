package sonar.logistics.base.data.generators.items;

import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.core.Api;
import appeng.me.helpers.IGridProxyable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import sonar.core.handlers.inventories.handling.ItemTransferHelper;
import sonar.logistics.api.asm.ASMTileInventoryProvider;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;

import javax.annotation.Nullable;

@ASMTileInventoryProvider(modid = "appliedenergistics2", priority = 0)
public class TileInventoryAE2 implements ITileInventoryProvider {

    @Nullable
    @Override
    public IItemHandler getHandler(TileEntity tile, EnumFacing face) {
        if(tile instanceof IGridProxyable) {
            return tile.getCapability(ItemTransferHelper.ITEM_HANDLER_CAPABILITY, face);
        }
        return null;
    }

    @Override
    public void getItemList(ItemChangeableList list, IItemHandler handler, TileEntity tile, EnumFacing face) {
        IGridNode gridNode = ((IGridProxyable) tile).getGridNode(AEPartLocation.fromFacing(face));
        if(gridNode != null) {
            IStorageGrid grid = gridNode.getGrid().getCache(IStorageGrid.class);
            IItemList<IAEItemStack> items = grid.getInventory(Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class)).getStorageList();
            items.forEach(i -> list.add(i.asItemStackRepresentation(), i.getStackSize(), i.getStackSize()));

        }
    }
}
