package sonar.logistics.info.providers;
/* FIXME
import java.util.List;

import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.logistics.api.asm.CustomTileHandler;
import sonar.logistics.api.info.ICustomTileHandler;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.info.register.LogicPath;
import sonar.logistics.connections.monitoring.MonitoredFluidStack;

@CustomTileHandler(handlerID = "bigreactors-coolant", modid = "bigreactors")
public class BigReactorsCoolantHandler implements ICustomTileHandler {

	@Override
	public boolean canProvideInfo(World world, IBlockState state, BlockPos pos, EnumFacing dir, TileEntity tile, Block block) {
		return tile instanceof TileEntityReactorPart;
	}

	@Override
	public void addInfo(ILogicInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, Block block, TileEntity tile) {
		TileEntityReactorPart reactorBase = (TileEntityReactorPart) tile;
		MultiblockReactor reactor = reactorBase.getReactorController();
		if (reactor != null && !reactor.isPassivelyCooled()) {
			Fluid vapor = reactor.getCoolantContainer().getVaporType();
			if (vapor != null) {
				infoList.add(new MonitoredFluidStack(new StoredFluidStack(vapor, reactor.getCoolantContainer().getVaporAmount())).setPath(currentPath.dupe()));
			}
			Fluid coolant = reactor.getCoolantContainer().getCoolantType();
			if (fluid != null) {
				infoList.add(new MonitoredFluidStack(new StoredFluidStack(fluid, reactor.getCoolantContainer().getCoolantAmount())).setPath(currentPath.dupe()));
			}
		}

	}

}
*/