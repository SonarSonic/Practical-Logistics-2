package sonar.logistics.network.sync;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncPart;
import sonar.logistics.api.core.tiles.nodes.NodeTransferMode;
import sonar.logistics.base.filters.EnumFilterListType;
import sonar.logistics.base.filters.IFluidFilter;
import sonar.logistics.base.filters.IItemFilter;
import sonar.logistics.base.filters.INodeFilter;
import sonar.logistics.core.tiles.readers.info.handling.InfoHelper;

import java.util.ArrayList;
import java.util.List;

public class SyncFilterList extends SyncPart {

	public List<INodeFilter> objs = new ArrayList<>();

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

	public boolean matches(ItemStack stack, NodeTransferMode mode) {
		boolean hasWhiteLists = false;
		boolean whitelisted = objs.isEmpty();
		for (INodeFilter filter : objs) {
			if (filter.getTransferMode().matches(mode) && filter instanceof IItemFilter) {
				IItemFilter itemFilter = (IItemFilter) filter;
				if (filter.getListType() == EnumFilterListType.BLACKLIST) {
					if (itemFilter.canTransferItem(stack)) {
						return false;
					}
				}
				if (filter.getListType() == EnumFilterListType.WHITELIST && !whitelisted) {
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
				if (filter.getListType() == EnumFilterListType.BLACKLIST) {
					if (fluidFilter.canTransferFluid(stack)) {
						return false;
					}
				}
				if (filter.getListType() == EnumFilterListType.WHITELIST && !whitelisted) {
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
			List newObjs = new ArrayList<>();
			NBTTagList tagList = nbt.getTagList(getTagName(), Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < tagList.tagCount(); i++) {
				newObjs.add(InfoHelper.readFilterFromNBT(tagList.getCompoundTagAt(i)));
			}
			objs = newObjs;
		} else if (nbt.getBoolean(getTagName() + "E")) {
			objs = new ArrayList<>();
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
		if (obj instanceof SyncFilterList) {
			return ((SyncFilterList) obj).getObjects().equals(this.objs);
		}
		return false;
	}
}
