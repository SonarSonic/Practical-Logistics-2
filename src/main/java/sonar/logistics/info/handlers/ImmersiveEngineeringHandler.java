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
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.register.LogicPath;
import sonar.logistics.api.register.RegistryType;
import sonar.logistics.info.types.LogicInfo;

@CustomTileHandler(handlerID = "immersiveengineering-progress", modid = "immersiveengineering")
public class ImmersiveEngineeringHandler implements ICustomTileHandler {

	@Override
	public boolean canProvideInfo(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof IProcessTile;
	}

	@Override
	public void addInfo(ILogicInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
		IProcessTile process = (IProcessTile) tile;
		int[] steps = process.getCurrentProcessesStep();
		int[] maxs = process.getCurrentProcessesMax();
		for (int i = 0; i < steps.length; i++) {
			int step = steps[i];
			int max = maxs[i];
			infoList.add(LogicInfo.buildDirectInfo(ClientNameConstants.CURRENT_PROCESS_TIME, i, RegistryType.TILE, step).setPath(currentPath.dupe()));
			infoList.add(LogicInfo.buildDirectInfo(ClientNameConstants.PROCESS_TIME, i, RegistryType.TILE, max).setPath(currentPath.dupe()));
		}
	}

}
