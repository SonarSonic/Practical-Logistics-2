package sonar.logistics.base.data.inventory;

import net.minecraft.item.ItemStack;
import sonar.logistics.base.data.api.IData;
import sonar.logistics.base.data.api.IDataCombinable;

import java.util.HashMap;
import java.util.Map;

public class InventoryData implements IData, IDataCombinable<InventoryData> {

    public Map<ItemStack, Long> inventory = new HashMap<>();
    public Map<ItemStack, Long> last_inventory = new HashMap<>();
    public long inventory_count = 0, inventory_max = 0;

    public void onGenerationStart(){
        last_inventory = inventory;
        inventory = new HashMap<>();
        inventory_count = 0;
        inventory_max = 0;
    }

    public void onGenerationFinish(){
        ///TODO CHECK CHANGES - DONE ELSEWHERE!
    }

    public void addStack(ItemStack stack, long count){
        if(stack.isEmpty() || count == 0){
            return;
        }
        for(Map.Entry<ItemStack, Long> entry : inventory.entrySet()){
            if(entry.getKey().isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, entry.getKey())){
                entry.setValue(entry.getValue()+count);
                return;
            }
        }
        inventory.put(stack, count);
    }

    public void addStorage(long count, long max){
        inventory_count += count;
        inventory_max += max;
    }

    @Override
    public boolean canCombine(IDataCombinable data) {
        return data instanceof InventoryData;
    }

    @Override
    public boolean doCombine(InventoryData data) {
        data.inventory.forEach(this::addStack);
        addStorage(data.inventory_count, inventory_max);
        return true;
    }
}
