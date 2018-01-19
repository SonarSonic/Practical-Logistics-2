package sonar.logistics.helpers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.api.multipart.IMultipart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import sonar.core.api.SonarAPI;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.utils.Pair;
import sonar.logistics.PL2;
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.filters.INodeFilter;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.lists.EnumListChange;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.lists.types.UniversalChangeableList;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.IScaleableDisplay;
import sonar.logistics.common.multiparts.TileLogistics;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.networking.channels.ItemNetworkChannels;
import sonar.logistics.packets.PacketItemInteractionText;

public class InfoHelper {

	public static boolean isMatchingInfo(IInfo info, IInfo info2) {
		return info.isMatchingType(info2) && info.isMatchingInfo(info2);
	}

	public static boolean isIdenticalInfo(IInfo info, IInfo info2) {
		return isMatchingInfo(info, info2) && info.isIdenticalInfo(info2);
	}

	public static final String DELETE = "del";
	public static final String SAVED = "saved";
	public static final String REMOVED = "rem";
	public static final String SYNCED = "spe";

	public static void screenItemStackClicked(int networkID, StoredItemStack storedItemStack, DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag) {
		Pair<Integer, ItemInteractionType> toRemove = getItemsToRemove(click.type);
		EnumFacing facing = displayInfo.container.getDisplay().getCableFace();
		ILogisticsNetwork network = PL2.getNetworkManager().getNetwork(networkID);
		if (toRemove.a != 0 && network.isValid()) {
			switch (toRemove.b) {
			case ADD:
				ItemStack stack = player.getHeldItem(player.getActiveHand());
				if (!stack.isEmpty()) {
					long changed = 0;
					if (!click.doubleClick) {
						changed = PL2API.getItemHelper().insertItemFromPlayer(player, network, player.inventory.currentItem);
					} else {
						changed = PL2API.getItemHelper().insertInventoryFromPlayer(player, network, player.inventory.currentItem);
					}
					if (changed > 0) {
						long itemCount = PL2API.getItemHelper().getItemCount(stack, network);
						PL2.network.sendTo(new PacketItemInteractionText(stack, itemCount, changed), (EntityPlayerMP) player);
						PacketHelper.createRapidItemUpdate(Lists.newArrayList(stack), networkID);
					}
				}
				break;
			case REMOVE:
				if (storedItemStack != null) {
					StoredItemStack extract = PL2API.getItemHelper().extractItem(network, storedItemStack.copy().setStackSize(toRemove.a));
					if (extract != null) {
						BlockPos pos = click.clickPos.offset(facing);
						long r = extract.stored;
						SonarAPI.getItemHelper().spawnStoredItemStack(extract, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), facing);
						long itemCount = PL2API.getItemHelper().getItemCount(storedItemStack.getItemStack(), network);
						PL2.network.sendTo(new PacketItemInteractionText(storedItemStack.getItemStack(), itemCount, -r), (EntityPlayerMP) player);
						PacketHelper.createRapidItemUpdate(Lists.newArrayList(storedItemStack.getItemStack()), networkID);
					}
				}
				break;
			default:
				break;
			}

		}
	}

	public static void onScreenFluidStackClicked(int networkID, StoredFluidStack fluidStack, DisplayScreenClick click, DisplayInfo displayInfo, EntityPlayer player, NBTTagCompound clickTag) {
		ILogisticsNetwork network = PL2.getNetworkManager().getNetwork(networkID);
		if (network.isValid()) {
			if (click.type == BlockInteractionType.RIGHT) {
				PL2API.getFluidHelper().drainHeldItem(player, network, click.doubleClick ? Integer.MAX_VALUE : 1000);
			} else if (fluidStack != null && click.type == BlockInteractionType.LEFT) {
				PL2API.getFluidHelper().fillHeldItem(player, network, fluidStack.copy().setStackSize(Math.min(fluidStack.stored, 1000)));
			} else if (fluidStack != null && click.type == BlockInteractionType.SHIFT_LEFT) {
				PL2API.getFluidHelper().fillHeldItem(player, network, fluidStack);
			}

		}
	}

	public static <T extends IInfo> NBTTagCompound writeMonitoredList(NBTTagCompound tag, AbstractChangeableList<T> stacks, SyncType type) {
		if (type.isType(SyncType.DEFAULT_SYNC)) {
			List<IMonitoredValue<T>> values = stacks.getList();
			if ((values == null || values.isEmpty())) {
				tag.setBoolean(DELETE, true);
				return tag;
			}
			NBTTagList list = new NBTTagList();
			for (IMonitoredValue<T> value : values) {
				EnumListChange change = value.getChange();
				if (change.shouldUpdate()) {
					NBTTagCompound compound = new NBTTagCompound();
					IInfo info = value.getSaveableInfo();
					list.appendTag(InfoHelper.writeInfoToNBT(compound, info, SyncType.SAVE));// change to sync so info can do it's update
					if (value.shouldDelete(change))
						compound.setBoolean(REMOVED, true);
				}
			}
			if (list.tagCount() != 0) {
				tag.setTag(SYNCED, list);
			}
		} else if (type.isType(SyncType.SAVE)) {
			NBTTagList list = new NBTTagList();
			List<IMonitoredValue<T>> values = stacks.getList();
			values.forEach(value -> list.appendTag(InfoHelper.writeInfoToNBT(new NBTTagCompound(), value.getSaveableInfo(), SyncType.SAVE)));
			tag.setTag(SAVED, list);
		}
		return tag;
	}

	// FIXME - to use updateWriting for some of the tags, like ILogicInfo
	/* public static <T extends IInfo> NBTTagCompound writeMonitoredList(NBTTagCompound tag, boolean lastWasNull, MonitoredList<T> stacks, SyncType type) { if (type.isType(SyncType.DEFAULT_SYNC)) { stacks.sizing.writeData(tag, SyncType.SAVE); NBTTagList list = new NBTTagList(); stacks.forEach(info -> { if (info != null && info.isValid()) { list.appendTag(InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE)); } }); if (list.tagCount() != 0) { tag.setTag(SYNC, list); return tag; } else { // if (!lastWasNull) tag.setBoolean(DELETE, true); return tag; } } else if (type.isType(SyncType.SPECIAL)) { if (!stacks.changed.isEmpty() || !stacks.removed.isEmpty()) { stacks.sizing.writeData(tag, SyncType.DEFAULT_SYNC); if ((stacks == null || stacks.isEmpty())) { if (!lastWasNull) tag.setBoolean(DELETE, true); return tag; } NBTTagList list = new NBTTagList(); for (int listType = 0; listType < 2; listType++) { List<T> stackList = listType == 0 ? stacks.changed : stacks.removed; for (int i = 0; i < stackList.size(); i++) { T info = stackList.get(i); if (info != null && info.isValid()) { NBTTagCompound compound = new NBTTagCompound(); compound.setBoolean(REMOVED, listType == 1); list.appendTag(InfoHelper.writeInfoToNBT(compound, info, SyncType.SAVE)); } } } if (list.tagCount() != 0) { tag.setTag(SPECIAL, list); } } } return tag; } */
	public static <L extends AbstractChangeableList> L readMonitoredList(NBTTagCompound tag, L stacks, SyncType type) {
		if (tag.hasKey(DELETE)) {
			stacks.values.clear();
			return stacks;
		}
		if (type.isType(SyncType.SAVE)) {
			if (!tag.hasKey(SAVED)) {
				return stacks;
			}
			NBTTagList list = tag.getTagList(SAVED, 10);
			stacks.values.clear();
			for (int i = 0; i < list.tagCount(); i++) {
				stacks.add(InfoHelper.readInfoFromNBT(list.getCompoundTagAt(i)));
			}
		} else if (type.isType(SyncType.DEFAULT_SYNC)) {
			if (!tag.hasKey(SYNCED)) {
				return stacks;
			}
			NBTTagList list = tag.getTagList(SYNCED, 10);
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound infoTag = list.getCompoundTagAt(i);
				boolean removed = infoTag.getBoolean(REMOVED);
				IInfo stack = InfoHelper.readInfoFromNBT(infoTag);
				IMonitoredValue value = stacks.find(stack);
				if (value == null) {
					if (!removed) {
						stacks.add(stack);
					}
				} else if (removed) {
					stacks.values.remove(value);
				} else {
					value.reset(stack);
				}

			}
		}
		return stacks;
	}

	public static AbstractChangeableList<IProvidableInfo> sortInfoList(AbstractChangeableList<IProvidableInfo> updateInfo) {
		Collections.sort(updateInfo.getList(), new Comparator<IMonitoredValue<IProvidableInfo>>() {
			public int compare(IMonitoredValue<IProvidableInfo> str1, IMonitoredValue<IProvidableInfo> str2) {
				return Integer.compare(str1.getSaveableInfo().getRegistryType().sortOrder, str2.getSaveableInfo().getRegistryType().sortOrder);
			}
		});
		List<IProvidableInfo> info = Lists.newArrayList();
		IProvidableInfo lastInfo = null;
		for (IMonitoredValue<IProvidableInfo> value : updateInfo.getList()) {
			IProvidableInfo blockInfo = value.getSaveableInfo();
			if (blockInfo != null && !blockInfo.isHeader()) {
				if (lastInfo == null || (!lastInfo.isHeader() && !lastInfo.getRegistryType().equals(blockInfo.getRegistryType()))) {
					info.add(LogicInfo.buildCategoryInfo(blockInfo.getRegistryType()));
				}
				info.add(value.getSaveableInfo());
				lastInfo = blockInfo;
			}
		}
		updateInfo.getList().clear();
		info.forEach(value -> updateInfo.add(value));

		return updateInfo;
	}

	public enum ItemInteractionType {
		ADD, REMOVE;
	}

	public static Pair<Integer, ItemInteractionType> getItemsToRemove(BlockInteractionType type) {
		switch (type) {
		case LEFT:
			return new Pair(1, ItemInteractionType.REMOVE);
		case RIGHT:
			return new Pair(64, ItemInteractionType.ADD);
		case SHIFT_LEFT:
			return new Pair(64, ItemInteractionType.REMOVE);
		default:
			return new Pair(0, ItemInteractionType.ADD);
		}
	}

	public static boolean hasInfoChanged(IInfo info, IInfo newInfo) {
		if (info == null && newInfo == null) {
			return false;
		} else if (info == null && newInfo != null || info != null && newInfo == null) {
			return true;
		}
		return info.isMatchingType(newInfo) && info.isMatchingInfo(newInfo) && info.isIdenticalInfo(newInfo);
	}

	public static int getName(String name) {
		return PL2ASMLoader.infoIds.get(name);
	}

	public static Class<? extends IInfo> getInfoType(int id) {
		return PL2ASMLoader.infoClasses.get(PL2ASMLoader.infoNames.get(id));
	}

	public static NBTTagCompound writeInfoToNBT(NBTTagCompound tag, IInfo info, SyncType type) {
		tag.setInteger("iiD", PL2ASMLoader.infoIds.get(info.getID()));
		info.writeData(tag, type);
		return tag;
	}

	public static IInfo readInfoFromNBT(NBTTagCompound tag) {
		return loadInfo(tag.getInteger("iiD"), tag);
	}

	public static IInfo loadInfo(int id, NBTTagCompound tag) {
		return NBTHelper.instanceNBTSyncable(getInfoType(id), tag);
	}

	public static INodeFilter readFilterFromNBT(NBTTagCompound tag) {
		return NBTHelper.instanceNBTSyncable(PL2ASMLoader.filterClasses.get(tag.getString("id")), tag);
	}

	public static NBTTagCompound writeFilterToNBT(NBTTagCompound tag, INodeFilter filter, SyncType type) {
		tag.setString("id", filter.getNodeID());
		filter.writeData(tag, type);
		return tag;
	}

}
