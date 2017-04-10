package sonar.logistics.info.providers;

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
import sonar.logistics.api.asm.TileInfoProvider;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;

@TileInfoProvider(handlerID = "ae2-grinder", modid = "appliedenergistics2")
public class AE2GrinderProvider implements ITileInfoProvider {

	@Override
	public boolean canProvide(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof TileGrinder;
	}

	@Override
	public void provide(IMasterInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
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
