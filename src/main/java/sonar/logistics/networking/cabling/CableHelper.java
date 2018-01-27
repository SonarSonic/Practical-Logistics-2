package sonar.logistics.networking.cabling;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

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
import sonar.core.utils.Pair;
import sonar.core.utils.SonarValidation;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.ICable;
import sonar.logistics.api.cabling.ICableConnectable;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.wrappers.CablingWrapper;
import sonar.logistics.common.multiparts.cables.TileDataCable;

public class CableHelper extends CablingWrapper {

	public static ICableConnectable getNetworkTile(ICable source, TileEntity tile, EnumFacing dir, boolean internal, boolean cableOnly) {
		IMultipartContainer container = null;
		if (!(tile instanceof IMultipartContainer)) {
			Optional<IMultipartContainer> cont = MultipartHelper.getContainer(tile.getWorld(), tile.getPos());
			container = cont.isPresent() ? cont.get() : null;
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

	public static ICableConnectable getConnection(ICable cable, EnumFacing dir, CableConnectionType type, boolean isInternal, boolean cableOnly) {
		if (type.matches(cable.canConnect(cable.getRegistryID(), cable.getConnectableType(), dir, isInternal))) {
			World world = cable.getCoords().getWorld();
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
	public static ICableConnectable getConnection(ICable cable, EnumFacing dir, CableConnectionType type, boolean cableOnly) {
		ICableConnectable internal = getConnection(cable, dir, type, true, cableOnly);
		return internal != null ? internal : getConnection(cable, dir, type, false, cableOnly);
	}
	
	public static CableRenderType getConnectionRenderType(ICable cable, EnumFacing dir) {
		ICableConnectable connection = getConnection(cable, dir, CableConnectionType.VISUAL, false);
		return connection == null ? CableRenderType.NONE : connection.getCableRenderSize(dir);
	}

	public static ConnectableType getConnectableType(ICable cable, EnumFacing dir) {
		ICableConnectable connection = getConnection(cable, dir, CableConnectionType.NETWORK, false);
		return getConnectableType(connection);
	}

	public static ConnectableType getConnectableType(ICableConnectable connection) {
		//return connection == null ? ConnectableType.NONE : (connection instanceof IDataCable ? ConnectableType.CONNECTABLE : ConnectableType.TILE);
		return connection == null ? ConnectableType.NONE : (connection instanceof ICable ? ((ICable) connection).getConnectableType() : ConnectableType.TILE);
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
	public static Pair<ConnectableType, Integer> getCableConnection(ICable source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		ICableConnectable external = getConnection(source, dir, CableConnectionType.NETWORK, false, true);
		ConnectableType type = getConnectableType(external);
		return new Pair(type, type == cableType ? ((ICable) external).getRegistryID() : -1);
	}

	public static TileDataCable getCable(IBlockAccess world, BlockPos pos) {
		IBlockAccess actualWorld = world;
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
		List<IInfoProvider> logicTiles = Lists.newArrayList();
		for (EnumFacing face : EnumFacing.values()) {
			ICableConnectable connect = getConnection(cable, face, CableConnectionType.VISUAL, false, false);
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
		List<T> logicTiles = Lists.newArrayList();
		for (EnumFacing face : EnumFacing.values()) {
			ICableConnectable connection = getConnection(cable, face, CableConnectionType.NETWORK, false);
			if (connection != null && !(connection instanceof IDataCable) && validate.isValid(connection)) {
				logicTiles.add((T) connection);
			}
		}
		return logicTiles;
	}
	/* public ILogisticsNetwork getNetwork(TileEntity tile, EnumFacing dir) { // watch out for this null :P Pair<ConnectableType, Integer> connection = PL2.getDisplayManager().getConnectionType(null, tile.getWorld(), tile.getPos(), dir, ConnectableType.CONNECTABLE); if (connection.a != ConnectableType.NONE && connection.b != -1) { ILogisticsNetwork cache = PL2.instance.networkManager.getNetwork(connection.b); if (cache != null) { return cache; } } return EmptyLogisticsNetwork.INSTANCE; } public ILogisticsNetwork getNetwork(int registryID) { return PL2.instance.networkManager.getNetwork(registryID); } */

	/* public static <T extends ICable> Pair<ConnectableType, Integer> getConnectionType(T source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) { BlockPos offset = pos.offset(dir); IMultipartContainer container = MultipartHelper.getPartContainer(world, offset); if (container != null) { return getConnectionType(source, container, dir, cableType); } else { TileEntity tile = world.getTileEntity(offset); if (tile != null) { return getConnectionTypeFromObject(source, tile, dir, cableType); } } return new Pair(ConnectableType.NONE, -1); } /** checks what cable type can be connected via a certain direction, assumes the other block can connect from this side public static <T extends ICable> Pair<ConnectableType, Integer> getConnectionType(T source, IMultipartContainer container, EnumFacing dir, ConnectableType cableType) { ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(dir.getOpposite())); if (part != null) { return getConnectionTypeFromObject(source, part, dir, cableType); } else { ISlottedPart centre = container.getPartInSlot(PartSlot.CENTER); if (centre != null && centre instanceof IDataCable) { return getConnectionTypeFromObject(source, centre, dir, cableType); } } return new Pair(ConnectableType.NONE, -1); } public static <T extends ICable> Pair<ConnectableType, Integer> getConnectionTypeFromObject(T source, Object connection, EnumFacing dir, ConnectableType cableType) { if (connection instanceof IDataCable) { IDataCable cable = (IDataCable) connection; if (cable.getConnectableType().canConnect(cableType)) { return cable.canConnectOnSide(cable.getRegistryID(), dir.getOpposite(), false) ? new Pair(cable.getConnectableType(), cable.getRegistryID()) : new Pair(ConnectableType.NONE, -1); } } else if (connection instanceof INetworkTile) { return ((INetworkTile) connection).canConnect(dir.getOpposite()).canShowConnection() ? new Pair(ConnectableType.TILE, -1) : new Pair(ConnectableType.NONE, -1); } return new Pair(ConnectableType.NONE, -1); } */
}
