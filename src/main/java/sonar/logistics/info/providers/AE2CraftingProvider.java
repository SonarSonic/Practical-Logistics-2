package sonar.logistics.info.providers;

import appeng.api.storage.data.IAEItemStack;
import appeng.tile.crafting.TileCraftingMonitorTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.integration.AE2Helper;
import sonar.logistics.api.asm.TileInfoProvider;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.info.register.LogicPath;
import sonar.logistics.info.types.MonitoredItemStack;

import java.util.List;

@TileInfoProvider(handlerID = "ae2-drive", modid = "appliedenergistics2")
public class AE2CraftingProvider implements ITileInfoProvider {

	@Override
	public boolean canProvide(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof TileCraftingMonitorTile;
	}

	@Override
	public void provide(IMasterInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
		TileCraftingMonitorTile monitorTile = (TileCraftingMonitorTile) tile;
		IAEItemStack stack = monitorTile.getJobProgress();
		infoList.add(new MonitoredItemStack(stack != null ? AE2Helper.convertAEItemStack(monitorTile.getJobProgress()) : new StoredItemStack(new ItemStack(Blocks.AIR, 0))).setPath(currentPath.dupe())); //needs to be called what's crafting somehow!!!!
	}
}
