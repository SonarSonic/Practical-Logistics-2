package sonar.logistics.base.filters;

import net.minecraft.item.ItemStack;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.tiles.IChannelledTile;
import sonar.logistics.network.sync.SyncFilterList;

import java.util.function.Predicate;

public interface IFilteredTile extends IChannelledTile, ILogicListenable {

	SyncFilterList getFilters();
	
	int getSlotID();

	Predicate<ItemStack> getFilter();
	
}
