package sonar.logistics.core.tiles.connections.redstone.handling;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.logistics.PL2Blocks;
import sonar.logistics.PL2Properties;
import sonar.logistics.api.core.tiles.connections.EnumCableConnection;
import sonar.logistics.api.core.tiles.connections.ICableConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneCable;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneConnectable;
import sonar.logistics.core.tiles.connections.data.handling.CableConnectionHelper;
import sonar.logistics.core.tiles.connections.redstone.tiles.BlockRedstoneCable;
import sonar.logistics.core.tiles.connections.redstone.tiles.TileRedstoneCable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RedstoneConnectionHelper {

	public static int getPower(IBlockState state, World world, BlockPos pos, EnumFacing face) {
		IBlockState iblockstate1 = world.getBlockState(pos);
		return iblockstate1.getBlock().shouldCheckWeakPower(iblockstate1, world, pos, face) ? world.getStrongPower(pos) : iblockstate1.getWeakPower(world, pos, face);

	}

	public static List<IRedstoneConnectable> getConnectables(IRedstoneCable cable) {
		List<IRedstoneConnectable> logicTiles = new ArrayList<>();
		for (EnumFacing face : EnumFacing.values()) {
			ICableConnectable connection = CableConnectionHelper.getConnection(cable, face, EnumCableConnection.NETWORK, false);
			if (connection != null && !(connection instanceof IRedstoneCable) && connection instanceof IRedstoneConnectable) {
				logicTiles.add((IRedstoneConnectable) connection);
			}
		}
		return logicTiles;
	}

	public static TileRedstoneCable getCable(IBlockAccess world, BlockPos pos) {
		IBlockAccess actualWorld = SonarMultipartHelper.unwrapBlockAccess(world);
		TileEntity tile = actualWorld.getTileEntity(pos);
		if (tile != null) {
			if (tile instanceof TileRedstoneCable) {
				return (TileRedstoneCable) tile;
			} else {
				Optional<IMultipartTile> cable = MultipartHelper.getPartTile(actualWorld, pos, EnumCenterSlot.CENTER);
				if (cable.isPresent() && cable.get() instanceof TileRedstoneCable) {
					return (TileRedstoneCable) cable.get();
				}
			}
		}
		return null;
	}

	public static IBlockState getCableState(IBlockAccess world, BlockPos pos) {
		IBlockAccess actualWorld = SonarMultipartHelper.unwrapBlockAccess(world);
		IBlockState state = actualWorld.getBlockState(pos);
		if (state.getBlock() instanceof BlockRedstoneCable) {
			return state;
		} else {
			Optional<IBlockState> cable = MultipartHelper.getPartState(actualWorld, pos, EnumCenterSlot.CENTER);
			if (cable.isPresent() && cable.get().getBlock() instanceof BlockRedstoneCable) {
				return cable.get();
			}
		}
		return PL2Blocks.redstone_cable.getDefaultState().withProperty(PL2Properties.ACTIVE, false);
	}
}
