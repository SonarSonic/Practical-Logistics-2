package sonar.logistics.base.data.types.inventory;

import net.minecraft.item.ItemStack;
import sonar.logistics.api.core.tiles.displays.info.lists.EnumListChange;
import sonar.logistics.base.data.api.IData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InventoryData implements IData {

    public Map<ItemStack, ItemCountData> inventory = new HashMap<>();

    public long inventory_count = 0, inventory_max = 0;

    public boolean hasUpdated = false;

    @Override
    public void preUpdate(){
        Iterator<ItemCountData> iterator = inventory.values().iterator();
        while(iterator.hasNext()){
            ItemCountData count = iterator.next();
            if(count.listChange.shouldDelete()){
                iterator.remove();
            }
            count.preUpdate();

        }
        inventory_count = 0;
        inventory_max = 0;
        hasUpdated = false;
    }

    @Override
    public void postUpdate(){
        Iterator<ItemCountData> iterator = inventory.values().iterator();
        while(iterator.hasNext()){
            ItemCountData count = iterator.next();
            count.postUpdate();
            if(count.listChange.shouldUpdate()){
                hasUpdated = true;
            }
        }
    }

    @Override
    public void onUpdated(){
        hasUpdated = false;
    }

    @Override
    public boolean hasUpdated(){
        return hasUpdated;
    }

    @Nullable
    public ItemCountData findCount(ItemStack stack){
        if(stack.isEmpty()){
            return null;
        }
        for(Map.Entry<ItemStack, ItemCountData> entry : inventory.entrySet()){
            if(entry.getKey().isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, entry.getKey())){
                return entry.getValue();
            }
        }
        return null;
    }

    public void addStack(ItemStack stack, long count){
        if(stack.isEmpty() || count == 0){
            return;
        }
        ItemCountData countData = findCount(stack);
        if(countData != null){
            countData.count+=count;
            return;
        }
        inventory.put(stack, new ItemCountData(count));
    }

    public void removeStack(ItemStack stack, long count){
        if(stack.isEmpty() || count == 0){
            return;
        }
        ItemCountData countData = findCount(stack);
        if(countData != null){
            countData.count-=count;
        }
    }

    public void setStack(ItemStack stack, long count){
        if(stack.isEmpty() || count == 0){
            return;
        }
        for(Map.Entry<ItemStack, ItemCountData> entry : inventory.entrySet()){
            if(entry.getKey().isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, entry.getKey())){
                entry.getValue().count=count;
                return;
            }
        }
        inventory.put(stack, new ItemCountData(count));
    }

    public void addStorage(long count, long max){
        inventory_count += count;
        inventory_max += max;
    }

    public String toString(){
        return inventory.toString();
    }

    public static class ItemCountData{
        public long lastCount;
        public long count;
        public EnumListChange listChange = EnumListChange.NEW_VALUE;

        public ItemCountData(long count){
            this.count = count;
        }

        public void preUpdate(){
            this.lastCount = count;
            this.count = 0;
        }

        public void postUpdate(){
            this.listChange = EnumListChange.getChange(count, lastCount);
        }

        public String toString(){
            return "Count: " + count + "Change: " + listChange.name();
        }

    }

}
