package sonar.logistics.networking.sorters;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import sonar.core.SonarCore;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.SonarHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.utils.SortingDirection;
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.lists.IMonitoredValue;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.tiles.readers.ILogicListSorter;
import sonar.logistics.api.tiles.readers.InventoryReader;
import sonar.logistics.api.tiles.readers.EnergyReader;
import sonar.logistics.api.tiles.readers.FluidReader.SortingType;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.MonitoredEnergyStack;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;

public class SortingHelper {

	public static AbstractChangeableList<IProvidableInfo> sortInfo(AbstractChangeableList<IProvidableInfo> updateInfo) {
		Collections.sort(updateInfo.getList(), new Comparator<IMonitoredValue<IProvidableInfo>>() {
			public int compare(IMonitoredValue<IProvidableInfo> str1, IMonitoredValue<IProvidableInfo> str2) {
				return Integer.compare(str1.getSaveableInfo().getRegistryType().sortOrder, str2.getSaveableInfo().getRegistryType().sortOrder);
			}
		});
		List<IProvidableInfo> info = new ArrayList<>();
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

	public static AbstractChangeableList<MonitoredItemStack> sortItems(AbstractChangeableList<MonitoredItemStack> updateInfo, SortingDirection direction, InventoryReader.SortingType type) {
		updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredItemStack>>() {
			public int compare(IMonitoredValue<MonitoredItemStack> str1, IMonitoredValue<MonitoredItemStack> str2) {
				StoredItemStack item1 = str1.getSaveableInfo().getStoredStack(), item2 = str2.getSaveableInfo().getStoredStack();
				return SonarHelper.compareStringsWithDirection(item1.getItemStack().getDisplayName(), item2.getItemStack().getDisplayName(), direction);
			}
		});

		switch (type) {
		case STORED:
			updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredItemStack>>() {
				public int compare(IMonitoredValue<MonitoredItemStack> str1, IMonitoredValue<MonitoredItemStack> str2) {
					StoredItemStack item1 = str1.getSaveableInfo().getStoredStack(), item2 = str2.getSaveableInfo().getStoredStack();
					return SonarHelper.compareWithDirection(item1.stored, item2.stored, direction);
				}
			});
			break;
		case MODID:
			updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredItemStack>>() {
				public int compare(IMonitoredValue<MonitoredItemStack> str1, IMonitoredValue<MonitoredItemStack> str2) {
					StoredItemStack item1 = str1.getSaveableInfo().getStoredStack(), item2 = str2.getSaveableInfo().getStoredStack();
					String modid1 = item1.getItemStack().getItem().getRegistryName().getResourceDomain();
					String modid2 = item2.getItemStack().getItem().getRegistryName().getResourceDomain();
					return SonarHelper.compareStringsWithDirection(modid1, modid2, direction);
				}
			});
		default:
			break;
		}
		return updateInfo;
	}

	public static AbstractChangeableList<MonitoredFluidStack> sortFluids(AbstractChangeableList<MonitoredFluidStack> updateInfo, final SortingDirection dir, SortingType type) {
		updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredFluidStack>>() {
			public int compare(IMonitoredValue<MonitoredFluidStack> str1, IMonitoredValue<MonitoredFluidStack> str2) {
				StoredFluidStack flu1 = str1.getSaveableInfo().getStoredStack(), flu2 = str2.getSaveableInfo().getStoredStack();
				int res = String.CASE_INSENSITIVE_ORDER.compare(flu1.getFullStack().getLocalizedName(), flu2.getFullStack().getLocalizedName());
				if (res == 0) {
					res = flu1.getFullStack().getLocalizedName().compareTo(flu2.getFullStack().getLocalizedName());
				}
				return dir == SortingDirection.DOWN ? res : -res;
			}
		});

		updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredFluidStack>>() {
			public int compare(IMonitoredValue<MonitoredFluidStack> str1, IMonitoredValue<MonitoredFluidStack> str2) {
				StoredFluidStack flu1 = str1.getSaveableInfo().getStoredStack(), flu2 = str2.getSaveableInfo().getStoredStack();
				switch (type) {
				case MODID:
					String modid1 = FluidRegistry.getModId(flu1.getFullStack());
					String modid2 = FluidRegistry.getModId(flu2.getFullStack());
					return SonarHelper.compareStringsWithDirection(modid1 == null ? PL2Constants.MINECRAFT : modid1, modid2 == null ? PL2Constants.MINECRAFT : modid2, dir);
				case NAME:
					break;
				case STORED:
					return SonarHelper.compareWithDirection(flu1.stored, flu2.stored, dir);
				case TEMPERATURE:
					return SonarHelper.compareWithDirection(flu1.getFullStack().getFluid().getTemperature(), flu2.getFullStack().getFluid().getTemperature(), dir);
				}
				return 0;
			}
		});
		return updateInfo;
	}

	public static AbstractChangeableList<MonitoredEnergyStack> sortEnergy(AbstractChangeableList<MonitoredEnergyStack> updateInfo, final SortingDirection dir, EnergyReader.SortingType type) {
		updateInfo.getList().sort(new Comparator<IMonitoredValue<MonitoredEnergyStack>>() {
			public int compare(IMonitoredValue<MonitoredEnergyStack> str1, IMonitoredValue<MonitoredEnergyStack> str2) {
				StoredEnergyStack item1 = str1.getSaveableInfo().getEnergyStack(), item2 = str2.getSaveableInfo().getEnergyStack();
				switch (type) {
				case CAPACITY:
					return SonarHelper.compareWithDirection(item1.capacity, item2.capacity, dir);
				case INPUT:
					return SonarHelper.compareWithDirection(item1.input, item2.input, dir);
				case NAME:
					String modid1 = str1.getSaveableInfo().getMonitoredCoords().getUnlocalizedName();
					String modid2 = str2.getSaveableInfo().getMonitoredCoords().getUnlocalizedName();
					return SonarHelper.compareStringsWithDirection(modid1, modid2, dir);
				case STORED:
					return SonarHelper.compareWithDirection(item1.stored, item2.stored, dir);
				case TYPE:
					return SonarHelper.compareStringsWithDirection(item1.energyType.getName(), item2.energyType.getName(), dir);
				}
				return 0;
			}
		});
		return updateInfo;
	}

	public static ILogicListSorter copySorter(ILogicListSorter sorter) {
		NBTTagCompound saveTag = saveListSorter(new NBTTagCompound(), sorter, SyncType.SAVE);
		return loadListSorter(saveTag);
	}

	//// LOAD AND SAVE SORTERS \\\\

	public static int getRegisteredID(ILogicListSorter info) {
		if (info == null || info.getRegisteredName() == null) {
			return -1;
		}
		Integer id = PL2ASMLoader.changeableListSorterIDs.get(info.getRegisteredName());
		return id == null ? -1 : id;
	}

	public static Class<? extends ILogicListSorter> getElementClass(int id) {
		return PL2ASMLoader.changeableListSorterIClasses.get(id);
	}

	public static NBTTagCompound saveListSorter(NBTTagCompound tag, ILogicListSorter info, SyncType type) {
		tag.setInteger("LsiD", getRegisteredID(info));
		return info.writeData(tag, type);
	}

	public static ILogicListSorter loadListSorter(NBTTagCompound tag) {
		int elementID = tag.getInteger("LsiD");
		return instanceListSorter(getElementClass(elementID), tag);
	}

	@Nullable
	public static <T extends ILogicListSorter> T instanceListSorter(Class<T> classType, NBTTagCompound tag) {
		T obj = null;
		try {
			if (classType != null) {
				obj = classType.getConstructor().newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			SonarCore.logger.error("FAILED TO CREATE NEW INSTANCE OF " + classType.getSimpleName());
		}
		if (obj != null) {
			obj.readData(tag, SyncType.SAVE);
			return obj;
		}
		return null;
	}

}
