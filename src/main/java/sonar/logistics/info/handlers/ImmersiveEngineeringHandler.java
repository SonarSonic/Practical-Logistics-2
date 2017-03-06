package sonar.logistics.info.handlers;

import java.util.List;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.api.asm.CustomTileHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.info.LogicInfoRegistry.LogicPath;
import sonar.logistics.info.LogicInfoRegistry.RegistryType;
import sonar.logistics.info.types.LogicInfo;

@CustomTileHandler(handlerID = "immersiveengineering", modid = "immersiveengineering")
public class ImmersiveEngineeringHandler implements ICustomTileHandler {

	@Override
	public boolean canProvideInfo(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof IProcessTile;
	}

	@Override
	public void addInfo(List<LogicInfo> infoList, LogicPath currentPath, World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		if (tile instanceof IProcessTile) {
			IProcessTile process = (IProcessTile) tile;
			int[] steps = process.getCurrentProcessesStep();
			int[] maxs = process.getCurrentProcessesMax();
			for (int i = 0; i < steps.length; i++) {
				int step = steps[i];
				int max = maxs[i];
				infoList.add(LogicInfo.buildDirectInfo("IProcessMachine.getCurrentProcessTime", i, RegistryType.TILE, step, currentPath.dupe()));
				infoList.add(LogicInfo.buildDirectInfo("IProcessMachine.getProcessTime", i, RegistryType.TILE, max, currentPath.dupe()));
			}
		}
	}

}
