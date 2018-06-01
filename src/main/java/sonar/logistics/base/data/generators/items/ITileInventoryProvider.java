package sonar.logistics.base.data.generators.items;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;
import sonar.logistics.core.tiles.displays.info.types.items.ItemChangeableList;

import javax.annotation.Nullable;

public interface ITileInventoryProvider {

    @Nullable
    IItemHandler getHandler(TileEntity tile, EnumFacing face);

    void getItemList(ItemChangeableList list, IItemHandler handler, TileEntity tile, EnumFacing face);

}
