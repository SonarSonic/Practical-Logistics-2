package sonar.logistics.common.multiparts.cables;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.helpers.FontHelper;
import sonar.core.utils.LabelledAxisAlignedBB;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.PL2Properties.PropertyCableFace;
import sonar.logistics.api.networks.EmptyLogisticsNetwork;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.api.utils.CacheType;
import sonar.logistics.common.multiparts.BlockLogistics;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.networking.CacheHandler;
import sonar.logistics.networking.connections.CableConnectionHandler;

public class BlockDataCable extends BlockLogistics {

	public BlockDataCable() {
		super(PL2Multiparts.DATA_CABLE);
	}

	public double p = 0.0625;
	public LabelledAxisAlignedBB cableBox = new LabelledAxisAlignedBB(6 * p, 6 * p, 6 * p, 1 - 6 * p, 1 - 6 * p, 1 - 6 * p).labelAxis("c");

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return cableBox;
	}

	public List<AxisAlignedBB> getSelectionBoxes(World world, BlockPos pos, List<AxisAlignedBB> collidingBoxes) {
		collidingBoxes.add(cableBox);
		
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

	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entityIn, isActualState);
		List<AxisAlignedBB> boxes = getSelectionBoxes(world, pos, Lists.newArrayList());
		boxes.forEach(box -> addCollisionBoxToList(pos, entityBox, collidingBoxes, box));

	}

	@Deprecated
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		TileDataCable cable = CableHelper.getCable(world, pos);
		if (cable != null && !world.isRemote) {
			CableConnectionHandler.instance().onNeighbourBlockStateChanged(cable, pos, fromPos);
		}
	}

	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		TileDataCable cable = CableHelper.getCable(world, pos);
		if (cable != null && !cable.getWorld().isRemote) {
			CableConnectionHandler.instance().onNeighbourTileEntityChanged(cable, pos, neighbor);
		}
	}

	public void onPartAdded(IPartInfo part, IPartInfo otherPart) {
		super.onPartAdded(part, otherPart);
		if (!part.getActualWorld().isRemote && part.getTile()!=null && part.getTile() instanceof IDataCable) {
			CableConnectionHandler.instance().onNeighbourMultipartAdded(((IDataCable) part.getTile()), part, otherPart);
		}
	}

	public void onPartRemoved(IPartInfo part, IPartInfo otherPart) {
		super.onPartRemoved(part, otherPart);
		if (!part.getActualWorld().isRemote && part.getTile()!=null && part.getTile() instanceof IDataCable) {
			CableConnectionHandler.instance().onNeighbourMultipartRemoved(((IDataCable) part.getTile()), part, otherPart);
		}
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		/*
		TileDataCable cable = PL2API.getCableHelper().getCable(world, pos);
		if (cable != null && !world.isRemote) {
			ILogisticsNetwork network = cable.getNetwork();
			FontHelper.sendMessage("Has network: " + !(network instanceof EmptyLogisticsNetwork), world, player);
			FontHelper.sendMessage("ID: " + cable.registryID, world, player);
			FontHelper.sendMessage(network.getCachedTiles(CacheHandler.TILE, CacheType.ALL).toString(), world, player);
		}
		*/
		return false;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		
		TileDataCable cable = CableHelper.getCable(world, pos);
		if (cable == null) {
			return state;
		}
		for (PropertyCableFace p : PL2Properties.PROPS) {
			//state = state.withProperty(p, cable.getRenderType(p.face));
			//FIXME make the cache work
			state = state.withProperty(p, CableHelper.getConnectionRenderType(cable, p.face)); ///cable.getRenderType(p.face));
		}		
		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = this.getDefaultState();
		for (PropertyCableFace p : PL2Properties.PROPS) {
			state = state.withProperty(p, CableRenderType.NONE);
		}
		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PL2Properties.PROPS);
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
