package sonar.logistics.network.sync;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncPart;
import sonar.logistics.api.filters.FilterList;
import sonar.logistics.api.filters.IFluidFilter;
import sonar.logistics.api.filters.IItemFilter;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.tiles.nodes.NodeTransferMode;
import sonar.logistics.helpers.InfoHelper;

public class SyncFilterList extends SyncPart {

	public List<INodeFilter> objs = Lists.newArrayList();

	public SyncFilterList(int id) {
		super(id);
	}

	public List<INodeFilter> getObjects() {
		return objs;
	}

	public void setObjects(List<INodeFilter> list) {
		objs = list;
		markChanged();
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
		boolean hasWhiteLists = false;
		boolean whitelisted = objs.isEmpty();
		for (INodeFilter filter : objs) {
			if (filter.getTransferMode().matches(mode) && filter instanceof IFluidFilter) {
				IFluidFilter fluidFilter = (IFluidFilter) filter;
				if (filter.getListType() == FilterList.BLACKLIST) {
					if (fluidFilter.canTransferFluid(stack)) {
						return false;
					}
				}
				if (filter.getListType() == FilterList.WHITELIST && !whitelisted) {
					hasWhiteLists=true;
					whitelisted = fluidFilter.canTransferFluid(stack);
				}
			}
		}
		return !hasWhiteLists || whitelisted;
	}
	
	public void addObject(INodeFilter object) {
		if (!objs.contains(object)) {
			objs.add(object);
			markChanged();
		}
	}

	public void removeObject(INodeFilter object) {
		if (objs.contains(object)) {
			objs.remove(object);
			markChanged();
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
			List newObjs = Lists.newArrayList();
			NBTTagList tagList = nbt.getTagList(getTagName(), Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				newObjs.add(InfoHelper.readFilterFromNBT(tagList.getCompoundTagAt(i)));
			}
			objs = newObjs;
		} else if (nbt.getBoolean(getTagName() + "E")) {
			objs = Lists.newArrayList();
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
