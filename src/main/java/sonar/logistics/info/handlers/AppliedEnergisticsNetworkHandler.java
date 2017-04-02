package sonar.logistics.info.handlers;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipe;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.me.storage.CellInventory;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.storage.TileDrive;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.network.sync.SyncTagType;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.CustomTileHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.info.types.AE2DriveInfo;

@CustomTileHandler(handlerID = "appeng2-network", modid = "appliedenergistics2")
public class AppliedEnergisticsNetworkHandler implements ICustomTileHandler {

	@Override
	public boolean canProvideInfo(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof TileGrinder || tile instanceof TileDrive;
	}

	@Override
	public void addInfo(ILogicInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {

		if (tile instanceof TileGrinder) {
			TileGrinder grinder = (TileGrinder) tile;
			int requiredTurns = 8;
			ItemStack processing = grinder.getStackInSlot(6);
			if (grinder.getStackInSlot(6) != null) {
				IGrinderRecipe r = AEApi.instance().registries().grinder().getRecipeForInput(processing);
				if (r != null) {
					requiredTurns = r.getRequiredTurns();
				}
			}
			registry.buildInfo(infoList, currentPath.dupe(), "TileGrinder", "requiredTurns", RegistryType.TILE, requiredTurns);
		} else if (tile instanceof TileDrive) {
			TileDrive drives = (TileDrive) tile;
			List<AE2DriveInfo> allInfo = Lists.newArrayList();

			long totalBytes = 0;
			long usedBytes = 0;
			long totalTypes = 0;
			long usedTypes = 0;
			long itemCount = 0;
			for (int i = 0; i < drives.getInternalInventory().getSizeInventory(); i++) {
				ItemStack is = drives.getInternalInventory().getStackInSlot(i);
				List<IMEInventoryHandler> handlers = Lists.newArrayList();
				if (is != null) {
					IMEInventoryHandler itemInventory = AEApi.instance().registries().cell().getCellInventory(is, null, StorageChannel.ITEMS);
					IMEInventoryHandler fluidInventory = AEApi.instance().registries().cell().getCellInventory(is, null, StorageChannel.FLUIDS);
					handlers = Arrays.asList(itemInventory, fluidInventory);
				}
				AE2DriveInfo info = new AE2DriveInfo(handlers, i+1).setPath(currentPath.dupe());
				totalBytes += info.totalBytes.getObject();
				usedBytes += info.usedBytes.getObject();
				totalTypes += info.totalTypes.getObject();
				usedTypes += info.usedTypes.getObject();
				itemCount += info.itemCount.getObject();				
				allInfo.add(info);
			}
			AE2DriveInfo driveInfo = new AE2DriveInfo().setPath(currentPath.dupe());
			driveInfo.totalBytes.setObject(totalBytes);
			driveInfo.usedBytes.setObject(usedBytes);
			driveInfo.totalTypes.setObject(totalTypes);
			driveInfo.usedTypes.setObject(usedTypes);
			driveInfo.itemCount.setObject(itemCount);
			infoList.add(driveInfo);
			infoList.addAll(allInfo);
			
		}

	}

}
