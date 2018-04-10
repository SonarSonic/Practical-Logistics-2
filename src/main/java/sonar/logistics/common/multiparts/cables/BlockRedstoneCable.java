package sonar.logistics.common.multiparts.cables;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.helpers.RayTraceHelper;
import sonar.logistics.PL2Blocks;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.PL2Properties.PropertyCableFace;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.IRedstoneCable;
import sonar.logistics.api.cabling.IRedstoneConnectable;
import sonar.logistics.common.multiparts.BlockLogistics;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.networking.cabling.RedstoneCableHelper;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;

public class BlockRedstoneCable extends BlockLogistics {

	public BlockRedstoneCable() {
		super(PL2Multiparts.REDSTONE_CABLE);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return PL2Properties.cableBox;
	}

	public static List<AxisAlignedBB> getSelectionBoxes(World world, BlockPos pos, List<AxisAlignedBB> collidingBoxes) {
		collidingBoxes.add(PL2Properties.cableBox);

		TileRedstoneCable cable = RedstoneCableHelper.getCable(world, pos);
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

	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, isActualState);
		List<AxisAlignedBB> boxes = getSelectionBoxes(world, pos, new ArrayList<>());
		boxes.forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		return RayTraceHelper.rayTraceBoxes(pos, start, end, getSelectionBoxes(world, pos, new ArrayList<>())).getLeft();
	}

	@Deprecated
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		TileRedstoneCable cable = RedstoneCableHelper.getCable(world, pos);
		IBlockState adjState = world.getBlockState(fromPos);
		if (cable != null && !cable.getWorld().isRemote && adjState.getBlock() != PL2Blocks.redstone_cable) {
			RedstoneConnectionHandler.instance().onNeighbourBlockStateChanged(cable, pos, fromPos);
		}
	}

	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		TileRedstoneCable cable = RedstoneCableHelper.getCable(world, pos);
		IBlockState state = world.getBlockState(neighbor);
		if (cable != null && !cable.getWorld().isRemote && state.getBlock() != PL2Blocks.redstone_cable) {
			RedstoneConnectionHandler.instance().onNeighbourTileEntityChanged(cable, pos, neighbor);
		}
	}

	public void onPartAdded(IPartInfo part, IPartInfo otherPart) {
		super.onPartAdded(part, otherPart);
		if (!part.getActualWorld().isRemote && part.getTile() != null && otherPart.getTile() != null && (otherPart.getTile() instanceof IRedstoneConnectable)) {
			RedstoneConnectionHandler.instance().onNeighbourMultipartAdded((IRedstoneCable) part.getTile(), (IRedstoneConnectable) otherPart.getTile());
		}
	}

	public void onPartRemoved(IPartInfo part, IPartInfo otherPart) {
		super.onPartRemoved(part, otherPart);
		if (!part.getActualWorld().isRemote && part.getTile() != null && otherPart.getTile() != null && (otherPart.getTile() instanceof IRedstoneConnectable)) {
			RedstoneConnectionHandler.instance().onNeighbourMultipartRemoved((IRedstoneCable) part.getTile(), (IRedstoneConnectable) otherPart.getTile());
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileRedstoneCable cable = RedstoneCableHelper.getCable(world, pos);
		if (cable == null) {
			return state;
		}
		for (PropertyCableFace p : PL2Properties.CABLE_FACES) {
			state = state.withProperty(p, CableHelper.getConnectionRenderType(cable, p.face));
		}
		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = this.getDefaultState();
		for (PropertyCableFace p : PL2Properties.CABLE_FACES) {
			state = state.withProperty(p, CableRenderType.NONE);
		}
		return state.withProperty(PL2Properties.ACTIVE, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PL2Properties.ACTIVE) ? 1 : 0;
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PL2Properties.ACTIVE, PL2Properties.NORTH, PL2Properties.EAST, PL2Properties.SOUTH, PL2Properties.WEST, PL2Properties.DOWN, PL2Properties.UP);
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
		return new TileRedstoneCable();
	}

}
