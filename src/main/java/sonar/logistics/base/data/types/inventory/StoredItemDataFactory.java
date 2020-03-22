package sonar.logistics.base.data.types.inventory;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.logistics.base.data.api.IDataFactory;

public class StoredItemDataFactory implements IDataFactory<StoredItemData> {

    public static final String COUNT_KEY = "pl_count";

    @Override
    public StoredItemData create() {
        return new StoredItemData();
    }

    @Override
    public void save(StoredItemData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = new NBTTagCompound();
        data.stack.writeToNBT(nbt);
        nbt.setLong(COUNT_KEY, data.countData.count);
        tag.setTag(key, nbt);
    }

    @Override
    public void read(StoredItemData data, String key, NBTTagCompound tag) {
        NBTTagCompound nbt = tag.getCompoundTag(key);
        data.stack = new ItemStack(nbt);
        data.countData.count = tag.getLong(COUNT_KEY);

    }

    @Override
    public void saveUpdate(StoredItemData data, ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, data.stack);
        buf.writeLong(data.countData.count);
    }

    @Override
    public void readUpdate(StoredItemData data, ByteBuf buf) {
        data.stack = ByteBufUtils.readItemStack(buf);
        data.countData.count = buf.readLong();
    }
}
