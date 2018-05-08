package sonar.logistics.integration.immersiveengineering;
/*FIXME
import java.util.List;

import blusunrize.immersiveengineering.base.blocks.IEBlockInterfaces.IProcessTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.api.asm.ASMTileInfoProvider;
import sonar.logistics.api.info.ClientNameConstants;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.handlers.ITileInfoProvider;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.info.register.LogicPath;
import sonar.logistics.api.info.register.RegistryType;
import sonar.logistics.core.base.displays.info.types.info.LogicInfo;

@ASMTileInfoProvider(handlerID = "immersiveengineering-progress", modid = "immersiveengineering")
public class ImmersiveEngineeringProvider implements ITileInfoProvider {

	@Override
	public boolean canProvide(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof IProcessTile;
	}

	@Override
	public void provide(IMasterInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
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
*/