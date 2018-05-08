package sonar.logistics.core.tiles.displays.info.types.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.network.sync.SyncNBTAbstract;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMInfo;
import sonar.logistics.api.core.tiles.displays.info.*;
import sonar.logistics.api.core.tiles.displays.info.comparators.ComparableObject;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValueInfo;
import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;
import sonar.logistics.api.core.tiles.displays.info.register.RegistryType;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IElementStorageHolder;
import sonar.logistics.core.tiles.displays.info.types.BaseInfo;

import java.util.List;

@ASMInfo(id = MonitoredItemStack.id, modid = PL2Constants.MODID)
public class MonitoredItemStack extends BaseInfo<MonitoredItemStack> implements IProvidableInfo<MonitoredItemStack>, IJoinableInfo<MonitoredItemStack>, INameableInfo<MonitoredItemStack>, IComparableInfo<MonitoredItemStack>, IMonitoredValueInfo<MonitoredItemStack> {

	public static final String id = "items";
	public final SyncNBTAbstract<StoredItemStack> itemStack = new SyncNBTAbstract<>(StoredItemStack.class, 0);
	public final SyncTagType.INT networkID = (INT) new SyncTagType.INT(1).setDefault(-1);
	{
		syncList.addParts(itemStack, networkID);
	}

	public MonitoredItemStack() {}

	public MonitoredItemStack(StoredItemStack stack, int networkID) {
		this(stack);
		this.networkID.setObject(networkID);
	}

	public MonitoredItemStack(StoredItemStack stack) {
		this.itemStack.setObject(stack);
	}

	@Override
	public boolean isIdenticalInfo(MonitoredItemStack info) {
		return getStoredStack().equals(info.getStoredStack());
	}

	@Override
	public boolean isMatchingInfo(MonitoredItemStack info) {
		return getStoredStack().equalStack(info.getStoredStack().getItemStack()) && networkID.getObject().equals(info.networkID.getObject());
	}

	@Override
	public boolean isMatchingType(IInfo info) {
		return info instanceof MonitoredItemStack;
	}

	@Override
	public boolean canJoinInfo(MonitoredItemStack info) {
		return isMatchingInfo(info);
	}

	@Override
	public IJoinableInfo joinInfo(MonitoredItemStack info) {
		itemStack.getObject().add(info.itemStack.getObject());
		return this;
	}

	@Override
	public boolean isValid() {
		return itemStack.getObject() != null && itemStack.getObject().item != null;
	}

	@Override
	public String getID() {
		return id;
	}

	public String toString() {
		if (itemStack.getObject() != null)
			return itemStack.getObject().toString();
		return super.toString() + " : NULL";
	}

	public ItemStack getItemStack() {
		return this.itemStack.getObject().getItemStack();
	}

	public StoredItemStack getStoredStack() {
		return this.itemStack.getObject();
	}

	public long getStored() {
		return this.itemStack.getObject().stored;
	}

	public int getNetworkSource() {
		return networkID.getObject();
	}

	public void setNetworkSource(int id) {
		networkID.setObject(id);
	}

	@Override
	public MonitoredItemStack copy() {
		return new MonitoredItemStack(itemStack.getObject().copy(), networkID.getObject());
	}

	@Override
	public String getClientIdentifier() {
		return "Item: " + (itemStack.getObject() != null && itemStack.getObject().getItemStack() != null ? itemStack.getObject().getItemStack().getDisplayName() : "ITEMSTACK");
	}

	@Override
	public String getClientObject() {
		return itemStack.getObject() != null ? "" + itemStack.getObject().stored : "ERROR";
	}

	@Override
	public String getClientType() {
		return "items";
	}

	@Override
	public void createDefaultElements(List<IDisplayElement> toAdd, IElementStorageHolder h, InfoUUID uuid) {
		toAdd.add(new NetworkItemElement(uuid));
	}

	@Override
	public List<ComparableObject> getComparableObjects(List<ComparableObject> objects) {
		StoredItemStack stack = itemStack.getObject();
		objects.add(new ComparableObject(this, "Stored", stack.stored));
		objects.add(new ComparableObject(this, "Damage", stack.getItemDamage()));
		objects.add(new ComparableObject(this, "NBT", stack.item.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound()));
		return objects;

	}

	@Override
	public RegistryType getRegistryType() {
		return RegistryType.TILE; // may want to be something different, may need to be configurable - tile would be the most likely, but not necessarily correct
	}

	@Override
	public MonitoredItemStack setRegistryType(RegistryType type) {
		return this;
	}

	@Override
	public void setFromReturn(LogicPath path, Object returned) {}

	public static MonitoredItemStack findItemStack(List<MonitoredItemStack> stacks, ItemStack item) {
		for (MonitoredItemStack i : stacks) {
			if (i.getStoredStack().equalStack(item)) {
				return i;
			}
		}
		return null;
	}

	@Override
	public IMonitoredValue<MonitoredItemStack> createMonitoredValue() {
		return new ItemCount(this);
	}

}