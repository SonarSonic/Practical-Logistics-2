package sonar.logistics.base.data.generators;

import sonar.logistics.base.data.api.IDataGenerator;
import sonar.logistics.base.data.api.methods.IMethod;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.methods.VanillaMethods;
import sonar.logistics.base.data.types.inventory.InventoryData;
import sonar.logistics.base.data.types.items.ItemHandlerData;

import java.util.List;

public class InventoryDataGenerator implements IDataGenerator<InventoryData> {

    public InventoryDataGenerator(){}

    @Override
    public Class<InventoryData> getDataType() {
        return InventoryData.class;
    }

    @Override
    public IMethod getDataMethod() {
        return VanillaMethods.INVENTORY_CAPABILITY;
    }

    public void generateData(InventoryData inventoryData, List<DataHolder> validHolders){
        inventoryData.preUpdate();
        for (DataHolder holder : validHolders) {
            ItemHandlerData data = (ItemHandlerData) holder.data;
            data.stackList.forEach(stack -> inventoryData.addStack(stack, stack.getCount()));
            inventoryData.addStorage(data.inventory_count, data.inventory_max);
        }
        inventoryData.postUpdate();
    }

    public boolean isValidHolder(DataHolder holder){
        return holder.data instanceof ItemHandlerData;
    }
}
