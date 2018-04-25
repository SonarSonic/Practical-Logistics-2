package sonar.logistics.common.multiparts.cables;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RayTraceHelper;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.PL2Properties.PropertyCableFace;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.common.multiparts.BlockLogistics;
import sonar.logistics.networking.cabling.CableConnectionHandler;
import sonar.logistics.networking.cabling.CableHelper;

public class BlockDataCable extends BlockLogistics {
	
	public BlockDataCable() {
		super(PL2Multiparts.DATA_CABLE);
	}

	@Nonnull
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return PL2Properties.cableBox;
	}

	public static List<AxisAlignedBB> getSelectionBoxes(World world, BlockPos pos, List<AxisAlignedBB> collidingBoxes) {
		collidingBoxes.add(PL2Properties.cableBox);
		
		TileDataCable cable = CableHelper.getCable(world, pos);
		if (cable != null) {
			for (EnumFacing face : EnumFacing.values()) {
				CableRenderType connect = cable.getRenderType(face);
				if (connect.canConnect()) {
					collidingBoxes.add(PL2Properties.getCableBox(connect, face));
				}
			}
		}
		
		return collidingBoxes;
	}

	public void addCollisionBoxToList(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, isActualState);
		List<AxisAlignedBB> boxes = getSelectionBoxes(world, pos, new ArrayList<>());
		boxes.forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));
	}
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
    	return RayTraceHelper.rayTraceBoxes(pos, start, end, getSelectionBoxes(world,pos, new ArrayList<>())).getLeft();
    }

	@Deprecated
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		TileDataCable cable = CableHelper.getCable(world, pos);
		if (cable != null && !world.isRemote) {
			CableConnectionHandler.instance().queueCableUpdate(cable);
		}
	}

	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		TileDataCable cable = CableHelper.getCable(world, pos);
		if (cable != null && !cable.getWorld().isRemote) {
			CableConnectionHandler.instance().queueCableUpdate(cable);
		}
	}

	public void onPartChanged(IPartInfo part, IPartInfo otherPart) {
		if (!part.getActualWorld().isRemote && part.getTile()!=null && part.getTile() instanceof IDataCable) {
			CableConnectionHandler.instance().queueCableUpdate(((IDataCable) part.getTile()));
		}
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!world.isRemote){
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof TileDataCable){
				FontHelper.sendMessage("ID: " + ((TileDataCable)tile).registryID.getObject(), world, player);
			}
		}
		return false;
	}

	@Nonnull
    @Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
		TileDataCable cable = CableHelper.getCable(world, pos);
		if (cable == null) {
			return state;
		}
		for (PropertyCableFace p : PL2Properties.CABLE_FACES) {
			state = state.withProperty(p, CableHelper.getConnectionRenderType(cable, p.face));
		}	
		return state;
	}

	@Nonnull
    @Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = this.getDefaultState();
		for (PropertyCableFace p : PL2Properties.CABLE_FACES) {
			state = state.withProperty(p, CableRenderType.NONE);
		}
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Nonnull
    public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PL2Properties.CABLE_FACES);
	}

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
		return EnumCenterSlot.CENTER;
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
		return EnumCenterSlot.CENTER;
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileDataCable();
	}

}
