package sonar.logistics.base.data.types.inventory;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.logistics.base.data.api.IDataFactory;

import java.util.HashMap;
import java.util.Map;

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
    public void save(InventoryData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        data.inventory.forEach((S,L) -> list.appendTag(saveStack(S,L)));
        nbt.setTag(INVENTORY_KEY, list);

        nbt.setLong(INV_COUNT_KEY, data.inventory_count);
        nbt.setLong(INV_MAX_KEY, data.inventory_max);
        tag.setTag(key, nbt);
    }

    @Override
    public void read(InventoryData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = tag.getCompoundTag(key);
        data.inventory.clear();
        NBTTagList list = nbt.getTagList(INVENTORY_KEY, Constants.NBT.TAG_COMPOUND);
        list.forEach(nbtBase -> readStack(data, (NBTTagCompound) nbtBase));

        data.inventory_count = nbt.getLong(INV_COUNT_KEY);
        data.inventory_max = nbt.getLong(INV_MAX_KEY);
    }

    @Override
    public void saveUpdate(InventoryData data, ByteBuf buf) {
        Map<ItemStack, InventoryData.ItemCountData> changed = new HashMap<>();
        data.inventory.forEach((S,L) -> {if(L.listChange.shouldUpdate()){changed.put(S,L);}});
        buf.writeInt(changed.size());
        if(!changed.isEmpty()){
            for(Map.Entry<ItemStack, InventoryData.ItemCountData> entry : changed.entrySet()){
                ByteBufUtils.writeItemStack(buf, entry.getKey());
                buf.writeLong(entry.getValue().count);
            }
        }
    }

    @Override
    public void readUpdate(InventoryData data, ByteBuf buf) {
        int changed = buf.readInt();
        int current_id = 0;
        while(current_id < changed){
            ItemStack stack = ByteBufUtils.readItemStack(buf);
            long count = buf.readLong();
            data.setStack(stack, count);
            current_id++;
        }
    }

    private NBTTagCompound saveStack(ItemStack stack, InventoryData.ItemCountData count){
        NBTTagCompound tag = new NBTTagCompound();
        stack.writeToNBT(tag);
        tag.setLong(COUNT_KEY, count.count);
        return tag;
    }

    public void readStack(InventoryData data, NBTTagCompound tag){
        ItemStack stack = new ItemStack(tag);
        long count = tag.getLong(COUNT_KEY);
        data.inventory.put(stack, new InventoryData.ItemCountData(count));
    }
}
