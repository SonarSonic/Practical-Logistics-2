package sonar.logistics.core.tiles.connections.data.handling;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.utils.Pair;
import sonar.core.utils.SonarValidation;
import sonar.logistics.api.core.tiles.connections.*;
import sonar.logistics.api.core.tiles.connections.data.IDataCable;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.api.core.tiles.displays.tiles.ILargeDisplay;
import sonar.logistics.api.core.tiles.readers.IInfoProvider;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.slots.EnumDisplayFaceSlot;
import sonar.logistics.core.tiles.connections.data.tiles.TileDataCable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CableConnectionHelper {

	public static ICableConnectable getNetworkTile(ICable source, TileEntity tile, EnumFacing dir, boolean internal, boolean cableOnly) {
		IMultipartContainer container = null;
		if (!(tile instanceof IMultipartContainer)) {
			Optional<IMultipartContainer> cont = MultipartHelper.getContainer(tile.getWorld(), tile.getPos());
			container = cont.orElse(null);
		} else {
			container = (IMultipartContainer) tile;
		}
		if (container != null) {
			if (!cableOnly) {// check side slot first if a cable isn't the only target
				Optional<IMultipartTile> part = container.getPartTile(EnumFaceSlot.fromFace(dir));
				if (part.isPresent() && part.get() instanceof ICableConnectable) {
					return (ICableConnectable) part.get();
				}
				if (!internal || !(source instanceof ILargeDisplay)) { // don't want the screen to return itself
					EnumFacing displaySlot = source instanceof ILargeDisplay ? ((ILargeDisplay) source).getCableFace() : dir;
					Optional<IMultipartTile> display = container.getPartTile(EnumDisplayFaceSlot.fromFace(displaySlot));
					if (display.isPresent() && display.get() instanceof ICableConnectable) {
						return (ICableConnectable) display.get();
					}
				}
			}
			if (!internal) { // don't want the cable to return itself
				Optional<IMultipartTile> cable = container.getPartTile(EnumCenterSlot.CENTER);
				if (cable.isPresent() && cable.get() instanceof ICableConnectable) {
					return (ICableConnectable) cable.get();
				}
			}
		} else if (!internal && tile instanceof ICableConnectable) {
			return (ICableConnectable) tile;
		}

		return null;
	}

	public static ICableConnectable getConnection(ICable cable, EnumFacing dir, EnumCableConnection type, boolean isInternal, boolean cableOnly) {
		if (type.matches(cable.canConnect(cable.getRegistryID(), cable.getConnectableType(), dir, isInternal))) {
			World world = cable.getActualWorld();
			TileEntity tile = world.getTileEntity(isInternal ? cable.getCoords().getBlockPos() : cable.getCoords().getBlockPos().offset(dir));
			if (tile != null) {
				EnumFacing actualDir = isInternal ? dir : dir.getOpposite();
				ICableConnectable connection = getNetworkTile(cable, tile, actualDir, isInternal, cableOnly);
				if (connection != null && type.matches(connection.canConnect(cable.getRegistryID(), cable.getConnectableType(), actualDir, isInternal))) {
					return connection;
				}
			}
		}
		return null;
	}

	@Nullable
	public static ICableConnectable getConnection(ICable cable, EnumFacing dir, EnumCableConnection type, boolean cableOnly) {
		ICableConnectable internal = getConnection(cable, dir, type, true, cableOnly);
		return internal != null ? internal : getConnection(cable, dir, type, false, cableOnly);
	}
	
	public static EnumCableRenderSize getConnectionRenderType(ICable cable, EnumFacing dir) {
		ICableConnectable connection = getConnection(cable, dir, EnumCableConnection.VISUAL, false);
		return connection == null ? EnumCableRenderSize.NONE : connection.getCableRenderSize(dir);
	}

	public static EnumCableConnectionType getConnectableType(ICable cable, EnumFacing dir) {
		ICableConnectable connection = getConnection(cable, dir, EnumCableConnection.NETWORK, false);
		return getConnectableType(connection);
	}

	public static EnumCableConnectionType getConnectableType(ICableConnectable connection) {
		//return connection == null ? EnumCableConnectionType.NONE : (connection instanceof IDataCable ? EnumCableConnectionType.CONNECTABLE : EnumCableConnectionType.TILE);
		return connection == null ? EnumCableConnectionType.NONE : (connection instanceof ICable ? ((ICable) connection).getConnectableType() : EnumCableConnectionType.TILE);
	}

	public static IDisplay getDisplay(World world, BlockPos pos, EnumDisplayFaceSlot slot) {
		IDisplay display = null;
		Optional<IMultipartTile> multipartTile = MultipartHelper.getPartTile(world, pos, slot);
		if (multipartTile.isPresent() && multipartTile.get() instanceof IDisplay) {
			display = (IDisplay) multipartTile.get();
		}
		return display;
	}

	/** to see if there is a neighbouring cable to connect to, (only checks external connections) */
	public static Pair<EnumCableConnectionType, Integer> getCableConnection(ICable source, World world, BlockPos pos, EnumFacing dir, EnumCableConnectionType cableType) {
		ICableConnectable external = getConnection(source, dir, EnumCableConnection.NETWORK, false, true);
		EnumCableConnectionType type = getConnectableType(external);
		return new Pair(type, type == cableType ? ((ICable) external).getRegistryID() : -1);
	}

	public static TileDataCable getCable(IBlockAccess world, BlockPos pos) {
		IBlockAccess actualWorld = SonarMultipartHelper.unwrapBlockAccess(world);
		TileEntity tile = actualWorld.getTileEntity(pos);
		if (tile != null) {
			if (tile instanceof TileDataCable) {
				return (TileDataCable) tile;
			} else {
				Optional<IMultipartTile> cable = MultipartHelper.getPartTile(actualWorld, pos, EnumCenterSlot.CENTER);
				if (cable.isPresent() && cable.get() instanceof TileDataCable) {
					return (TileDataCable) cable.get();
				}
			}
		}
		return null;
	}

	public static List<IInfoProvider> getLocalMonitors(IDataCable cable) {
		List<IInfoProvider> logicTiles = new ArrayList<>();
		for (EnumFacing face : EnumFacing.values()) {
			ICableConnectable connect = getConnection(cable, face, EnumCableConnection.VISUAL, false, false);
			if (connect instanceof IInfoProvider) {
				logicTiles.add((IInfoProvider) connect);
			}
		}
		return logicTiles;
	}

	public static List<INetworkTile> getConnectedTiles(IDataCable cable) {
		return getConnectedTiles(cable, new SonarValidation.CLASS(INetworkTile.class));
	}

	public static <T> List<T> getConnectedTiles(IDataCable cable, Class<T> type) {
		return getConnectedTiles(cable, new SonarValidation.CLASS(type));
	}

	public static List getConnectedTilesOfTypes(IDataCable cable, Class... type) {
		return getConnectedTiles(cable, new SonarValidation.CLASSLIST(type));
	}

	public static <T> List<T> getConnectedTiles(IDataCable cable, SonarValidation validate) {
		List<T> logicTiles = new ArrayList<>();
		for (EnumFacing face : EnumFacing.values()) {
			ICableConnectable connection = getConnection(cable, face, EnumCableConnection.NETWORK, false);
			if (connection != null && !(connection instanceof IDataCable) && validate.isValid(connection)) {
				logicTiles.add((T) connection);
			}
		}
		return logicTiles;
	}
	
	public static boolean canDisplayConnectToItemNetwork(InfoUUID uuid, IDisplay display, int networkID){
		//ILogisticsNetwork handling = LogisticsHelper.getLocalProviders(viewables, world, pos, part)
		
		return false;		
	}
}
