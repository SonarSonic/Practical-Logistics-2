package sonar.logistics.base.data.generators;

import net.minecraft.item.ItemStack;
import sonar.logistics.base.data.api.IDataGenerator;
import sonar.logistics.base.data.api.methods.IMethod;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.methods.VanillaMethods;
import sonar.logistics.base.data.types.inventory.StoredItemData;
import sonar.logistics.base.data.types.items.ItemHandlerData;

import java.util.List;

public class StoredItemDataGenerator implements IDataGenerator<StoredItemData> {

    public ItemStack stack;

    public StoredItemDataGenerator(ItemStack stack){
        this.stack = stack;
    }

    @Override
    public Class<StoredItemData> getDataType() {
        return StoredItemData.class;
    }

    @Override
    public IMethod getDataMethod() {
        return VanillaMethods.INVENTORY_CAPABILITY;
    }

    @Override
    public void generateData(StoredItemData itemData, List<DataHolder> validHolders) {
        itemData.preUpdate();
        for (DataHolder holder : validHolders) {
            ItemHandlerData data = (ItemHandlerData) holder.data;
            data.stackList.stream().filter(itemData::isItemType).forEach(S -> itemData.addStack(S, S.getCount()));
        }
        itemData.postUpdate();
    }

    @Override
    public boolean isValidHolder(DataHolder holder) {
        return holder.data instanceof ItemHandlerData;
    }
}
