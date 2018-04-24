package sonar.logistics.info.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.core.Api;
import appeng.tile.storage.TileDrive;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.api.asm.TileInfoProvider;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.info.types.AE2DriveInfo;

@TileInfoProvider(handlerID = "ae2-drive", modid = "appliedenergistics2")
public class AE2DriveProvider implements ITileInfoProvider {

	@Override
	public boolean canProvide(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof TileDrive;
	}

	@Override
	public void provide(IMasterInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
		TileDrive drives = (TileDrive) tile;
		List<AE2DriveInfo> allInfo = new ArrayList<>();
		long totalBytes = 0;
		long usedBytes = 0;
		long totalTypes = 0;
		long usedTypes = 0;
		long itemCount = 0;
		for (int i = 0; i < drives.getInternalInventory().getSlots(); i++) {
			ItemStack is = drives.getInternalInventory().getStackInSlot(i);
			List<IMEInventoryHandler> handlers = new ArrayList<>();
			if (!is.isEmpty()) {
				IMEInventoryHandler itemInventory = AEApi.instance().registries().cell().getCellInventory(is, null, Api.INSTANCE.storage().getStorageChannel(IItemStorageChannel.class)); //should ISaveProvider by null?
				IMEInventoryHandler fluidInventory = AEApi.instance().registries().cell().getCellInventory(is, null, Api.INSTANCE.storage().getStorageChannel(IFluidStorageChannel.class));
				handlers = Arrays.asList(itemInventory, fluidInventory);
			}
			AE2DriveInfo info = new AE2DriveInfo(handlers, i + 1).setPath(currentPath.dupe());
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
