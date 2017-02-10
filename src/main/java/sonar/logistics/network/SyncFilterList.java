package sonar.logistics.network;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncPart;
import sonar.logistics.api.filters.FilterList;
import sonar.logistics.api.filters.IFluidFilter;
import sonar.logistics.api.filters.IItemFilter;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.nodes.NodeTransferMode;
import sonar.logistics.helpers.InfoHelper;

/** for use with objects which implement INBTSyncable and have an Empty Constructor for instances */
public class SyncFilterList extends SyncPart {

	public ArrayList<INodeFilter> objs = new ArrayList();

	public SyncFilterList(int id) {
		super(id);
	}

	public ArrayList<INodeFilter> getObjects() {
		return objs;
	}

	public void setObjects(ArrayList<INodeFilter> list) {
		objs = list;
		markDirty();
	}

	public boolean matches(StoredItemStack stack, NodeTransferMode mode) {
		boolean hasWhiteLists = false;
		boolean whitelisted = objs.isEmpty();
		for (INodeFilter filter : objs) {
			if (filter.getTransferMode().matches(mode) && filter instanceof IItemFilter) {
				IItemFilter itemFilter = (IItemFilter) filter;
				if (filter.getListType() == FilterList.BLACKLIST) {
					if (itemFilter.canTransferItem(stack)) {
						return false;
					}
				}
				if (filter.getListType() == FilterList.WHITELIST && !whitelisted) {
					hasWhiteLists=true;
					whitelisted = itemFilter.canTransferItem(stack);
				}
			}
		}
		return !hasWhiteLists || whitelisted;
	}

	public boolean matches(StoredFluidStack stack, NodeTransferMode mode) {
		boolean whitelisted = false;
		for (INodeFilter filter : objs) {
			if (filter.getTransferMode().matches(mode) && filter instanceof IFluidFilter) {
				IFluidFilter fluidFilter = (IFluidFilter) filter;
				if (filter.getListType() == FilterList.BLACKLIST) {
					if (!fluidFilter.canTransferFluid(stack)) {
						return false;
					}

				}
				if (filter.getListType() == FilterList.WHITELIST && !whitelisted) {
					whitelisted = fluidFilter.canTransferFluid(stack);
				}
			}
		}
		return whitelisted;
	}

	public void addObject(INodeFilter object) {
		if (!objs.contains(object)) {
			objs.add(object);
			markDirty();
		}
	}

	public void removeObject(INodeFilter object) {
		if (objs.contains(object)) {
			objs.remove(object);
			markDirty();
		}
	}

	@Override
	public void writeToBuf(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, writeData(new NBTTagCompound(), SyncType.SAVE));
	}

	@Override
	public void readFromBuf(ByteBuf buf) {
		readData(ByteBufUtils.readTag(buf), SyncType.SAVE);
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		if (nbt.hasKey(getTagName())) {
			ArrayList newObjs = new ArrayList();
			NBTTagList tagList = nbt.getTagList(getTagName(), Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				newObjs.add(InfoHelper.readFilterFromNBT(tagList.getCompoundTagAt(i)));
			}
			objs = newObjs;
		} else if (nbt.getBoolean(getTagName() + "E")) {
			objs = new ArrayList();
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagList tagList = new NBTTagList();
		objs.forEach(obj -> {
			if (obj != null)
				tagList.appendTag(InfoHelper.writeFilterToNBT(new NBTTagCompound(), obj, SyncType.SAVE));
		});
		if (!tagList.hasNoTags()) {
			nbt.setTag(getTagName(), tagList);
		} else {
			nbt.setBoolean(getTagName() + "E", true);
		}
		return nbt;
	}

	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SyncFilterList) {
			return ((SyncFilterList) obj).getObjects().equals(this.objs);
		}
		return false;
	}
}
