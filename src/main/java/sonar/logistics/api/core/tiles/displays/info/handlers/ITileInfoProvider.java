package sonar.logistics.api.core.tiles.displays.info.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.register.IMasterInfoRegistry;
import sonar.logistics.api.core.tiles.displays.info.register.LogicPath;

import javax.annotation.Nullable;
import java.util.List;

public interface ITileInfoProvider {

	/** returns if this tile handler can provide info at the given position */
    boolean canProvide(World world, IBlockState state, BlockPos pos, EnumFacing dir, @Nullable TileEntity tile, @Nullable Block block);

	/** allows you to add all types of info for a given position for use in the Info Reader
	 * @param registry the master registry
	 * @param infoList the current info list
	 * @param currentPath the current path of the provided info. You should use LogicPath.dupe() when you create new LogicInfo with this ICustomTileHandler
	 * @param methodCode if this is null, provide all info, if it isn't null provide the method associated with it, this will increase efficiency if implemented correctly
	 * @param world the world
	 * @param state the current block state
	 * @param pos the position
	 * @param dir the direction to obtain info from
	 * @param tile the TileEntity (can be null)
	 * @param block the Block (can be null) */
    void provide(IMasterInfoRegistry registry, List<IProvidableInfo> infoList, LogicPath currentPath, Integer methodCode, World world, IBlockState state, BlockPos pos, EnumFacing dir, @Nullable Block block, @Nullable TileEntity tile);
}
