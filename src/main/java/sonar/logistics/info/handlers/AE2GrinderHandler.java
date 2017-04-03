package sonar.logistics.info.handlers;

import java.util.List;

import appeng.api.AEApi;
import appeng.api.features.IGrinderRecipe;
import appeng.tile.grindstone.TileGrinder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.api.asm.CustomTileHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;

@CustomTileHandler(handlerID = "ae2-grinder", modid = "appliedenergistics2")
public class AE2GrinderHandler implements ICustomTileHandler {

	@Override
	public boolean canProvideInfo(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof TileGrinder;
	}

	@Override
	public void addInfo(ILogicInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
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
	}

}
