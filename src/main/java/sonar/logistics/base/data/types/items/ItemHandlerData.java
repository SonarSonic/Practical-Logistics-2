package sonar.logistics.base.data.types.items;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import sonar.logistics.base.data.api.IData;

import java.util.HashMap;
import java.util.Map;

public class ItemHandlerData implements IData {

    public NonNullList<ItemStack> stackList = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);

    public Map<Integer, ItemStack> changedStacks = new HashMap();    ///server side changed slot && last stack

    public boolean hasCountChanged = false;
    public long inventory_count = 0, inventory_max = 0;
    public long last_count = 0, last_max = 0;

    public void preUpdate(){
        changedStacks.clear();
        hasCountChanged = false;
        last_count = inventory_count;
        last_max = inventory_max;
        inventory_count = 0;
        inventory_max = 0;
    }

    public void postUpdate(){
        if(inventory_count != last_count || inventory_max != last_max){
            hasCountChanged = true;
        }
    }

    public boolean hasUpdated(){
        return hasCountChanged || !changedStacks.isEmpty();
    }

}
