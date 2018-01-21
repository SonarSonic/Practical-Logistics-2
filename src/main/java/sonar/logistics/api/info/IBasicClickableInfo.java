package sonar.logistics.api.info;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

/** implemented on info which can be clicked by the player */
public interface IBasicClickableInfo{

	/** @param type the type of click
	 * @param doubleClick if the player double clicked
	 * @param renderInfo the infos current render properties
	 * @param player the player who clicked the info
	 * @param hand players hand
	 * @param stack players held item
	 * @param hit the RayTrace hit info
	 * @param container the displays info container
	 * @return if the screen was clicked */
	
	public boolean onStandardClick(TileAbstractDisplay part, DisplayInfo renderInfo, BlockInteractionType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ);			
}
