package sonar.logistics.info.providers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.TileInfoProvider;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.info.register.LogicPath;
import sonar.logistics.api.info.register.RegistryType;

import java.util.List;

@TileInfoProvider(handlerID = "normal-fluids", modid = PL2Constants.MODID)
public class NormalFluidProvider implements ITileInfoProvider {

	@Override
	public boolean canProvide(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return block instanceof BlockLiquid;
	}

	@Override
	public void provide(IMasterInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
		BlockLiquid liquid = (BlockLiquid) block;
		Fluid fluid = state.getMaterial() == Material.WATER ? FluidRegistry.WATER : FluidRegistry.LAVA;
		registry.getAssignableMethods(fluid.getClass(), RegistryType.BLOCK).forEach(method -> registry.getClassInfo(infoList, currentPath.dupe(), RegistryType.BLOCK, fluid, method, world, state, pos, dir, block, tile));
	}

}
