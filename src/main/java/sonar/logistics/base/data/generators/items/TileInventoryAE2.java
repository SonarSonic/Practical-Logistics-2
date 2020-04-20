package sonar.logistics.base.data.generators.items;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.core.Api;
import appeng.me.helpers.IGridProxyable;
import appeng.me.helpers.MachineSource;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import sonar.core.api.utils.ActionType;
import sonar.core.integration.AE2Helper;
import sonar.logistics.api.asm.ASMTileInventoryProvider;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ASMTileInventoryProvider(modid = "appliedenergistics2", priority = 0)
public class TileInventoryAE2 implements ITileInventoryProvider {

    @Nullable
    @Override
    public IItemHandler getHandler(TileEntity tile, EnumFacing face) {
        if(tile instanceof IGridProxyable) {
            IGridNode gridNode = ((IGridProxyable) tile).getGridNode(AEPartLocation.fromFacing(face));
            if(gridNode != null) {
                IStorageGrid grid = gridNode.getGrid().getCache(IStorageGrid.class);
                IMEMonitor<IAEItemStack> items = grid.getInventory(Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class));
                return new AE2ItemHandler((IActionHost)tile, items);
            }
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

    public static class AE2ItemHandler implements IItemHandler{

        public final IActionHost tile;
        public IMEMonitor<IAEItemStack> items;
        public IItemList<IAEItemStack> list;

        public AE2ItemHandler(IActionHost tile, IMEMonitor<IAEItemStack> items){
            this.tile = tile;
            this.items = items;
            this.list = items.getStorageList();
        }

        @Override
        public int getSlots() {
            return list.size()+1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if(slot == 0){
                return ItemStack.EMPTY; //to allow inserting
            }
            int i = 0;
            for(IAEItemStack item : list){
                if(i == slot - 1){
                    ItemStack stack = item.asItemStackRepresentation().copy();
                    stack.setCount(Math.min((int)Math.min(Integer.MAX_VALUE, item.getStackSize()), 64));
                    return stack;
                }
                i++;
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            IAEItemStack aeStack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
            if(aeStack != null){
                IAEItemStack injected = items.injectItems(aeStack, AE2Helper.getActionable(ActionType.getTypeForAction(simulate)), new MachineSource(tile));
                return injected != null ? injected.createItemStack().copy() : ItemStack.EMPTY;
            }
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(slot == 0){
                return ItemStack.EMPTY;
            }
            ItemStack stack = getStackInSlot(slot);
            stack.setCount(amount);
            IAEItemStack aeStack = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
            if(aeStack == null){
                return ItemStack.EMPTY;
            }
            IAEItemStack extracted = items.extractItems(aeStack, AE2Helper.getActionable(ActionType.getTypeForAction(simulate)), new MachineSource(tile));
            return extracted == null ? ItemStack.EMPTY : extracted.createItemStack().copy();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }
    }
}
