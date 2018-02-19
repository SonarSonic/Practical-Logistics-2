package sonar.logistics.common.items;

import java.util.Optional;

import mcmultipart.api.item.ItemBlockMultipart;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sonar.logistics.api.cabling.ICable;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.readers.BlockAbstractReader;
import sonar.logistics.networking.displays.DisplayHelper;

/** for multiparts which are placed onto cables, this automatically flips them so they face the most logical direction depending on the block clicked BEHAVIOUR: CLICK A CABLE: Placed on the side of the cable. CLICKED ON A BLOCK: Placed on the clicked blocks side. */
public class PL2ItemConnectableMultipart extends ItemBlockMultipart {

	public PL2ItemConnectableMultipart(Block block, IMultipart multipartBlock) {
		super(block, multipartBlock);
	}

	public <T extends Block & IMultipart> PL2ItemConnectableMultipart(T block) {
		super(block);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return place(player, world, pos, hand, facing, hitX, hitY, hitZ, this, this::getStateForConnectable, multipartBlock, this::placeBlockAtTested, ItemBlockMultipart::placePartAt);
	}

	public IBlockState getStateForConnectable(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		ICable cable  = null;
		Optional<IMultipartTile> tile = MultipartHelper.getPartTile(world, pos, EnumCenterSlot.CENTER);
		if (tile.isPresent() && tile.get() instanceof ICable) {
			cable = (ICable) tile.get();
		}
		
		if(this.multipartBlock instanceof BlockAbstractReader){
			Optional<IMultipartTile> screenTile = MultipartHelper.getPartTile(world, pos, EnumDisplayFaceSlot.fromFace(facing.getOpposite()));
			if(screenTile.isPresent() && screenTile.get() instanceof IDisplay){
				facing = ((IDisplay)screenTile.get()).getGSI().getFacing();
			}else if(cable == null){
				facing = DisplayHelper.getScreenOrientation(placer, facing)[0];
			}
		}
		return this.block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);		
	}


}
