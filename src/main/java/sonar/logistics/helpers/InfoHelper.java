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
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.IScaleableDisplay;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.common.multiparts2.TileLogistics;
import sonar.logistics.common.multiparts2.displays.TileAbstractDisplay;
import sonar.logistics.connections.channels.ItemNetworkChannels;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.network.PacketItemInteractionText;

public class InfoHelper {

	public static boolean isMatchingInfo(IInfo info, IInfo info2) {
		return info.isMatchingType(info2) && info.isMatchingInfo(info2);
	}

	public static boolean isIdenticalInfo(IInfo info, IInfo info2) {
		return isMatchingInfo(info, info2) && info.isIdenticalInfo(info2);
	}

	public static final String DELETE = "del";
	public static final String SYNC = "syn";
	public static final String REMOVED = "rem";
	public static final String SPECIAL = "spe";

	public static void screenItemStackClicked(StoredItemStack storedItemStack, TileAbstractDisplay part, DisplayInfo renderInfo, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Pair<Integer, ItemInteractionType> toRemove = getItemsToRemove(type);
		ILogisticsNetwork cache =  part.getNetwork();
		if (toRemove.a != 0 && cache.isValid()) {
			switch (toRemove.b) {
			case ADD:
				if (storedItemStack != null) {
					long changed = 0;
					if (!part.isDoubleClick()) {
						changed = PL2API.getItemHelper().insertItemFromPlayer(player, cache, player.inventory.currentItem);
					} else {
						changed = PL2API.getItemHelper().insertInventoryFromPlayer(player, cache, player.inventory.currentItem);
					}
					if (changed > 0) {
						long itemCount = PL2API.getItemHelper().getItemCount(storedItemStack, cache);
						PL2.network.sendTo(new PacketItemInteractionText(storedItemStack, itemCount, changed), (EntityPlayerMP) player);
						// FontHelper.sendMessage(TextFormatting.BLUE + "PL2: " + TextFormatting.RESET + "Stored " + itemCount + TextFormatting.GREEN + "+" + changed + TextFormatting.RESET + " x " + stack.getDisplayName(), player.getEntityWorld(), player);
					}
				}
				break;
			case REMOVE:
				if (storedItemStack != null) {
					IMultipart part = (IMultipart) hit.hitInfo;
					if (part != null && part instanceof TileLogistics) {
						BlockPos pos = hit.getBlockPos();
						StoredItemStack extract = PL2API.getItemHelper().extractItem(cache, storedItemStack.copy().setStackSize(toRemove.a));
						if (extract != null) {
							pos = pos.offset(hit.sideHit);
							long r = extract.stored;
							SonarAPI.getItemHelper().spawnStoredItemStack(extract, player.getEntityWorld(), pos.getX(), pos.getY(), pos.getZ(), hit.sideHit);
							long itemCount = PL2API.getItemHelper().getItemCount(storedItemStack.getItemStack(), cache);
							PL2.network.sendTo(new PacketItemInteractionText(storedItemStack.getItemStack(), itemCount, -r), (EntityPlayerMP) player);
						}
					}
				}
				break;
			default:
				break;
			}
			ItemNetworkChannels channels = cache.getNetworkChannels(ItemNetworkChannels.class);
			if (channels != null)
				channels.sendFullRapidUpdate();

		}
	}

	public static void onScreenFluidStackClicked(TileAbstractDisplay part, DisplayInfo renderInfo, BlockInteractionType type, StoredFluidStack fluidStack, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ILogisticsNetwork network = part.getNetwork();
		if (network.isValid()) {
			if (type == BlockInteractionType.RIGHT) {
				PL2API.getFluidHelper().drainHeldItem(player, network, part.isDoubleClick() ? Integer.MAX_VALUE : 1000);
			} else if (fluidStack != null && type == BlockInteractionType.LEFT) {
				PL2API.getFluidHelper().fillHeldItem(player, network, fluidStack.copy().setStackSize(Math.min(fluidStack.stored, 1000)));
			} else if (fluidStack != null && type == BlockInteractionType.SHIFT_LEFT) {
				PL2API.getFluidHelper().fillHeldItem(player, network, fluidStack);
			}

			ItemNetworkChannels channels = network.getNetworkChannels(ItemNetworkChannels.class);
			if (channels != null)
				channels.sendFullRapidUpdate();

		}
	}

	// FIXME - to use updateWriting for some of the tags, like ILogicInfo
	public static <T extends IInfo> NBTTagCompound writeMonitoredList(NBTTagCompound tag, boolean lastWasNull, MonitoredList<T> stacks, SyncType type) {
		if (type.isType(SyncType.DEFAULT_SYNC)) {
			stacks.sizing.writeData(tag, SyncType.SAVE);
			NBTTagList list = new NBTTagList();
			stacks.forEach(info -> {
				if (info != null && info.isValid()) {
					list.appendTag(InfoHelper.writeInfoToNBT(new NBTTagCompound(), info, SyncType.SAVE));
				}
			});
			if (list.tagCount() != 0) {
				tag.setTag(SYNC, list);
				return tag;
			} else {
				// if (!lastWasNull)
				tag.setBoolean(DELETE, true);
				return tag;
			}
		} else if (type.isType(SyncType.SPECIAL)) {
			if (!stacks.changed.isEmpty() || !stacks.removed.isEmpty()) {
				stacks.sizing.writeData(tag, SyncType.DEFAULT_SYNC);
				if ((stacks == null || stacks.isEmpty())) {
					if (!lastWasNull)
						tag.setBoolean(DELETE, true);
					return tag;
				}
				NBTTagList list = new NBTTagList();
				for (int listType = 0; listType < 2; listType++) {
					List<T> stackList = listType == 0 ? stacks.changed : stacks.removed;
					for (int i = 0; i < stackList.size(); i++) {
						T info = stackList.get(i);
						if (info != null && info.isValid()) {
							NBTTagCompound compound = new NBTTagCompound();
							compound.setBoolean(REMOVED, listType == 1);
							list.appendTag(InfoHelper.writeInfoToNBT(compound, info, SyncType.SAVE));
						}
					}
				}
				if (list.tagCount() != 0) {
					tag.setTag(SPECIAL, list);
				}
			}
		}
		return tag;
	}

	public static <T extends IInfo> MonitoredList<T> readMonitoredList(NBTTagCompound tag, MonitoredList<T> stacks, SyncType type) {
		if (tag.hasKey(DELETE)) {
			stacks.clear();
			return stacks;
		}
		if (type.isType(SyncType.DEFAULT_SYNC)) {
			if (!tag.hasKey(SYNC)) {
				return stacks;
			}
			NBTTagList list = tag.getTagList(SYNC, 10);
			stacks.clear();
			for (int i = 0; i < list.tagCount(); i++) {
				stacks.add((T) InfoHelper.readInfoFromNBT(list.getCompoundTagAt(i)));
			}
		} else if (type.isType(SyncType.SPECIAL)) {
			if (!tag.hasKey(SPECIAL)) {
				return stacks;
			}
			NBTTagList list = tag.getTagList(SPECIAL, 10);
			tags: for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound infoTag = list.getCompoundTagAt(i);
				boolean removed = infoTag.getBoolean(REMOVED);
				T stack = (T) InfoHelper.readInfoFromNBT(infoTag);
				Iterator<T> iterator = stacks.iterator();
				while (iterator.hasNext()) {
					T stored = iterator.next();
					if (stack.isMatchingType(stored) && stack.isMatchingInfo(stored)) {
						if (removed) {
							stacks.remove(stored);
						} else {
							stored.readData(infoTag, SyncType.SAVE);
						}
						continue tags;
					}
				}
				stacks.add(stack);
			}
		}
		return stacks;
	}

	public static List<IProvidableInfo> sortInfoList(List<IProvidableInfo> oldInfo) {
		List<IProvidableInfo> providerInfo = Lists.newArrayList(oldInfo);
		Collections.sort(providerInfo, new Comparator<IProvidableInfo>() {
			public int compare(IProvidableInfo str1, IProvidableInfo str2) {
				return Integer.compare(str1.getRegistryType().sortOrder, str2.getRegistryType().sortOrder);
			}
		});
		List<IProvidableInfo> sortedInfo = Lists.newArrayList();
		IProvidableInfo lastInfo = null;
		for (IProvidableInfo blockInfo : Lists.newArrayList(providerInfo)) {
			if (blockInfo != null && !blockInfo.isHeader()) {
				if (lastInfo == null || (!lastInfo.isHeader() && !lastInfo.getRegistryType().equals(blockInfo.getRegistryType()))) {
					sortedInfo.add(LogicInfo.buildCategoryInfo(blockInfo.getRegistryType()));
				}
				sortedInfo.add(blockInfo);
				lastInfo = blockInfo;
			}
		}
		return sortedInfo;
	}

	public static double[] getScaling(IDisplay display, DisplayLayout layout, int pos) {
		DisplayType type = display.getDisplayType();
		double width = type.width, height = type.height, scale = type.scale;
		if (display instanceof IScaleableDisplay) {
			double[] scaling = ((IScaleableDisplay) display).getScaling();
			width = scaling[0];
			height = scaling[1];
			scale = scaling[2];
		}

		switch (layout) {
		case DUAL:
			return new double[] { width, height / 2, scale };
		case GRID:
			return new double[] { width / 2, height / 2, scale / 1.5 };
		case LIST:
			return new double[] { width, height / 4, scale / 1.5 };
		default:
			return new double[] { width, height, scale * 1.2 };
		}
	}

	public static double[] getTranslation(IDisplay display, DisplayLayout layout, int pos) {
		DisplayType type = display.getDisplayType();
		double width = type.width, height = type.height, scale = type.scale;
		if (display instanceof IScaleableDisplay) {
			double[] scaling = ((IScaleableDisplay) display).getScaling();
			width = scaling[0];
			height = scaling[1];
			scale = scaling[2];
		}

		switch (layout) {
		case DUAL:
			return new double[] { 0, pos == 1 ? height / 2 : 0, 0 };
		case GRID:
			return new double[] { pos == 1 || pos == 3 ? (double) width / 2 : 0, (double) pos > 1 ? height / 2 : 0, 0 };
		case LIST:
			return new double[] { 0, pos * (height / 4), 0 };
		default:
			return new double[] { 0, 0, 0 };
		}
	}

	public static double[] getIntersect(IDisplay display, DisplayLayout layout, int pos) {
		DisplayType type = display.getDisplayType();
		double width = type.width, height = type.height, scale = type.scale;
		if (display instanceof IScaleableDisplay) {
			double[] scaling = ((IScaleableDisplay) display).getScaling();
			width = scaling[0];
			height = scaling[1];
			scale = scaling[2];
		}

		switch (layout) {
		case DUAL:
			return new double[] { 0, pos == 1 ? type.height / 2 : 0, pos == 1 ? 1 : type.width / 2, pos == 1 ? 1 : type.height / 2 };
		case GRID:
			return new double[] { (pos == 1 || pos == 3 ? type.width / 2 : 0), (pos == 1 || pos == 3 ? type.height / 2 : 0), (pos == 1 || pos == 3 ? 1 : type.width / 2), (pos == 1 || pos == 3 ? 1 : type.height / 2) };
		case LIST:
			return new double[] { 0, pos * (type.height / 4), 0, (pos + 1) * (type.height / 4) };

		default:
			return new double[] { 0, 0, type.width, type.height };
		}
	}

	public static boolean canBeClickedStandard(TileAbstractDisplay part, DisplayInfo renderInfo, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (renderInfo.container.getMaxCapacity() == 1) {
			return true;
		}
		IDisplay display = renderInfo.container.getDisplay();
		double[] intersect = getIntersect(display, display.getLayout(), renderInfo.getRenderProperties().infoPos);
		double x = pos.getX() + hitX;
		double y = pos.getY() + hitY;
		if (x >= intersect[0] && x <= intersect[2] && 1 - y >= intersect[1] && 1 - y <= intersect[3]) {
			return true;
		}
		return false;
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
