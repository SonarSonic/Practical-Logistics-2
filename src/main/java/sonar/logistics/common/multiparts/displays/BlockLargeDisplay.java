package sonar.logistics.common.multiparts.displays;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.displays.tiles.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockLargeDisplay extends BlockAbstractDisplay {

	public static final PropertyEnum<DisplayConnections> TYPE = PropertyEnum.create("type", DisplayConnections.class);

	public static final double depth = 0.0625, height = depth * 16, width = 0, length = depth * 1;
	public static final AxisAlignedBB DOWN_AXIS = new AxisAlignedBB(0, 0, 0, 1, length, 1);
	public static final AxisAlignedBB UP_AXIS = new AxisAlignedBB(0, 1, 0, 1, 1 - length, 1);
	public static final AxisAlignedBB NORTH_AXIS = new AxisAlignedBB((width) / 2, 0, length, 1 - width / 2, height, 0);
	public static final AxisAlignedBB SOUTH_AXIS = new AxisAlignedBB((width) / 2, 0, 1, 1 - width / 2, height, 1 - length);
	public static final AxisAlignedBB WEST_AXIS = new AxisAlignedBB(length, 0, (width) / 2, 0, height, 1 - width / 2);
	public static final AxisAlignedBB EAST_AXIS = new AxisAlignedBB(1, 0, (width) / 2, 1 - length, height, 1 - width / 2);
	public static final AxisAlignedBB[] AXIS = new AxisAlignedBB[] { DOWN_AXIS, UP_AXIS, NORTH_AXIS, SOUTH_AXIS, WEST_AXIS, EAST_AXIS };

	public BlockLargeDisplay() {
		super(PL2Multiparts.LARGE_DISPLAY_SCREEN);
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileLargeDisplayScreen) {
			TileLargeDisplayScreen source = ((TileLargeDisplayScreen) tile);
			ConnectedDisplay connectedDisplay = source.getConnectedDisplay();
			if(connectedDisplay == null){
				return false;
			}
			TileLargeDisplayScreen display = source.shouldRender.getObject() ? source : (TileLargeDisplayScreen) connectedDisplay.getTopLeftScreen();
			if (display != null) {
				if (facing != state.getValue(SonarProperties.ORIENTATION)) {
					if (!world.isRemote && canOpenGui(player)) {
						display.openFlexibleGui(player, 0);
					}
				} else {
					return display.getGSI().onClicked(display, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
				}
			}
		}
		return true;
	}

	//// STATE \\\\
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess w, BlockPos pos) {
		List<EnumFacing> faces = new ArrayList<>();
		TileEntity tile = w.getTileEntity(pos);
		if (tile instanceof TileLargeDisplayScreen) {
			TileLargeDisplayScreen screen = (TileLargeDisplayScreen) tile;
			for (EnumFacing face : EnumFacing.VALUES) {
				if (face == screen.getCableFace() || face == screen.getCableFace().getOpposite()) {
					continue;
				}
				IDisplay display = null;
				Optional<IMultipartTile> multipartTile = MultipartHelper.getPartTile(w, pos.offset(face), EnumDisplayFaceSlot.fromFace(screen.getCableFace()));
				if (multipartTile.isPresent() && multipartTile.get() instanceof IDisplay) {
					display = (IDisplay) multipartTile.get();
				}
				if (display != null && display.getDisplayType() == DisplayType.LARGE && ((ILargeDisplay) display).getRegistryID() == screen.getRegistryID()) {
					switch (display.getCableFace()) {
					case DOWN:
						EnumFacing toAdd = face;
						if (toAdd == EnumFacing.NORTH || toAdd == EnumFacing.SOUTH) {
							toAdd = toAdd.getOpposite();
						}
						faces.add(toAdd);
						break;
					case EAST:
						toAdd = face.rotateAround(Axis.Z).rotateAround(Axis.Y);
						if (toAdd == EnumFacing.NORTH || toAdd == EnumFacing.SOUTH) {
							toAdd = toAdd.getOpposite();
						}
						faces.add(toAdd);
						break;
					case NORTH:
						toAdd = face.rotateAround(Axis.Z).rotateAround(Axis.X).rotateAround(Axis.Y);
						if (toAdd == EnumFacing.NORTH || toAdd == EnumFacing.SOUTH) {
							toAdd = toAdd.getOpposite();
						}
						faces.add(toAdd);
						break;
					case SOUTH:
						toAdd = face.rotateAround(Axis.Z).rotateAround(Axis.X).rotateAround(Axis.Y).getOpposite();
						faces.add(toAdd);
						break;
					case UP:
						faces.add(face);
						break;
					case WEST:
						faces.add(face.rotateAround(Axis.Z).rotateAround(Axis.Y).getOpposite());
						break;
					default:
						break;

					}
				}

			}
		}
		DisplayConnections type = DisplayConnections.getType(faces);
		return state.withProperty(SonarProperties.ROTATION, EnumFacing.NORTH).withProperty(TYPE, type);
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AXIS[getOrientation(state).ordinal()];
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SonarProperties.ORIENTATION, SonarProperties.ROTATION, TYPE);
	}
}
