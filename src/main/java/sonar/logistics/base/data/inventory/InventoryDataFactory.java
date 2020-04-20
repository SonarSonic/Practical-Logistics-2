package sonar.logistics.base.data.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import sonar.logistics.base.data.api.IDataFactory;

public class InventoryDataFactory implements IDataFactory<InventoryData> {

    public static final String INVENTORY_KEY = "inv";
    public static final String COUNT_KEY = "pl_count";
    public static final String INV_COUNT_KEY = "inv_count";
    public static final String INV_MAX_KEY = "inv_max";

    @Override
    public InventoryData create() {
        return new InventoryData();
    }

    @Override
    public void save(InventoryData data, NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        data.inventory.forEach((S,L) -> list.appendTag(saveStack(S,L)));
        tag.setTag(INVENTORY_KEY, list);

        tag.setLong(INV_COUNT_KEY, data.inventory_count);
        tag.setLong(INV_MAX_KEY, data.inventory_max);
    }

    @Override
    public void read(InventoryData data, NBTTagCompound tag) {
        NBTTagList list = tag.getTagList(INVENTORY_KEY, Constants.NBT.TAG_COMPOUND);
        list.forEach(nbt -> readStack(data, (NBTTagCompound) nbt));

        data.inventory_count = tag.getLong(INV_COUNT_KEY);
        data.inventory_max = tag.getLong(INV_MAX_KEY);
    }

    private NBTTagCompound saveStack(ItemStack stack, Long count){
        NBTTagCompound tag = new NBTTagCompound();
        stack.writeToNBT(tag);
        tag.setLong(COUNT_KEY, count);
        return tag;
    }

    public void readStack(InventoryData data, NBTTagCompound tag){
        ItemStack stack = new ItemStack(tag);
        long count = tag.getLong(COUNT_KEY);
        data.inventory.put(stack, count);
    }
}
