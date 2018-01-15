package sonar.logistics.common.multiparts.displays;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.tiles.displays.DisplayConnections;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;

public class BlockLargeDisplay extends BlockAbstractDisplay {

	public static final PropertyEnum<DisplayConnections> TYPE = PropertyEnum.<DisplayConnections>create("type", DisplayConnections.class);

	public BlockLargeDisplay() {
		super(PL2Multiparts.LARGE_DISPLAY_SCREEN);
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack != null && stack.getItem() instanceof IOperatorTool) {
			return false;
		}
		if (canOpenGui(player) && !world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null && tile instanceof TileLargeDisplayScreen) {
				TileLargeDisplayScreen display = (TileLargeDisplayScreen) ((TileLargeDisplayScreen) tile).getDisplayScreen().getTopLeftScreen();
				if (facing != state.getValue(SonarProperties.ORIENTATION)) {
					display.openFlexibleGui(player, 0);
				} else {
					return display.container().onClicked(display, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
				}
			}
		}
		return true;
	}

	//// MULTIPART \\\\
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		double p = 0.0625;
		double height = p * 16, width = 0, length = p * 1;

		EnumFacing face = state.getValue(SonarProperties.ORIENTATION);
		switch (face) {
		case EAST:
			return new AxisAlignedBB(1, 0, (width) / 2, 1 - length, height, 1 - width / 2);
		case NORTH:
			return new AxisAlignedBB((width) / 2, 0, length, 1 - width / 2, height, 0);
		case SOUTH:
			return new AxisAlignedBB((width) / 2, 0, 1, 1 - width / 2, height, 1 - length);
		case WEST:
			return new AxisAlignedBB(length, 0, (width) / 2, 0, height, 1 - width / 2);
		case UP:
			return new AxisAlignedBB(0, 1 - 0, 0, 1, 1 - length, 1);
		default:
			return new AxisAlignedBB(0, 0, 0, 1, length, 1);

		}
	}

	//// STATE \\\\
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess w, BlockPos pos) {
		IBlockState currentState = state;
		List<EnumFacing> faces = Lists.newArrayList();
		TileEntity tile = w.getTileEntity(pos);
		if (tile != null && tile instanceof TileLargeDisplayScreen) {
			TileLargeDisplayScreen screen = (TileLargeDisplayScreen) tile;
			for (EnumFacing face : EnumFacing.VALUES) {
				if (face == screen.getCableFace() || face == screen.getCableFace().getOpposite()) {
					continue;
				}
				IDisplay display = null;
				Optional<IMultipartTile> multipartTile = MultipartHelper.getPartTile(w, pos.offset(face), EnumDisplayFaceSlot.fromFace(screen.getCableFace()));
				if(multipartTile.isPresent() && multipartTile.get() instanceof IDisplay){
					display = (IDisplay) multipartTile.get();
				}
				if (display != null && display.getDisplayType() == DisplayType.LARGE  && ((ILargeDisplay) display).getRegistryID() == screen.getRegistryID()) {
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
		return currentState.withProperty(SonarProperties.ROTATION, EnumFacing.NORTH).withProperty(TYPE, type);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SonarProperties.ORIENTATION, SonarProperties.ROTATION, TYPE });
	}
}
