package sonar.logistics.info.handlers;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.storage.TileDrive;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.integration.AE2Helper;
import sonar.logistics.api.asm.CustomTileHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.connections.monitoring.MonitoredItemStack;
import sonar.logistics.info.types.AE2DriveInfo;

@CustomTileHandler(handlerID = "ae2-drive", modid = "appliedenergistics2")
public class AE2CraftingHandler implements ICustomTileHandler {

	@Override
	public boolean canProvideInfo(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof TileCraftingMonitorTile;
	}

	@Override
	public void addInfo(ILogicInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
		TileCraftingMonitorTile monitorTile = (TileCraftingMonitorTile) tile;
		IAEItemStack stack = monitorTile.getJobProgress();
		infoList.add(new MonitoredItemStack((StoredItemStack) (stack != null ? AE2Helper.convertAEItemStack(monitorTile.getJobProgress()) : new StoredItemStack(new ItemStack(Blocks.AIR, 0)))).setPath(currentPath.dupe())); //needs to be called what's crafting somehow!!!!
	}
}
