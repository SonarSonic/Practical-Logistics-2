package sonar.logistics.common.multiparts2.displays;

import java.util.List;

import com.google.common.collect.Lists;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.common.block.properties.SonarProperties;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.tiles.displays.DisplayConnections;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;

public class BlockLargeDisplay extends BlockAbstractDisplay {

	public static final PropertyEnum<DisplayConnections> TYPE = PropertyEnum.<DisplayConnections>create("type", DisplayConnections.class);

	public BlockLargeDisplay(PL2Multiparts multipart) {
		super(multipart);
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
				IDisplay display = PL2API.getCableHelper().getDisplayScreen(BlockCoords.translateCoords(screen.getCoords(), face), screen.getCableFace());
				if (display != null && display.getDisplayType() == DisplayType.LARGE && ((ILargeDisplay) display).getRegistryID() == display.getNetworkID()) {
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
