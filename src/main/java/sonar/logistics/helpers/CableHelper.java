package sonar.logistics.helpers;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.utils.Pair;
import sonar.core.utils.SonarValidation;
import sonar.logistics.PL2;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.networks.EmptyLogisticsNetwork;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.ICable;
import sonar.logistics.api.tiles.INetworkConnection;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.wrappers.CablingWrapper;
import sonar.logistics.common.multiparts.cables.TileDataCable;

public class CableHelper extends CablingWrapper {

	public static INetworkConnection getNetworkTile(int networkID, TileEntity tile, EnumFacing dir, boolean internal, boolean cableOnly) {
		TileEntity actualTile = tile instanceof TileSonarMultipart && ((TileSonarMultipart) tile).info != null ? (TileEntity) ((TileSonarMultipart) tile).info.getContainer() : tile;
		
		
		if (actualTile instanceof IMultipartContainer) {
			IMultipartContainer container = (IMultipartContainer) actualTile;	
			
			if (!cableOnly) {// check side slot first if a cable isn't the only target
				Optional<IMultipartTile> part = container.getPartTile(EnumFaceSlot.fromFace(dir));
				if (part.isPresent() && part.get() instanceof INetworkConnection) {
					return (INetworkConnection) part.get();
				}
			}
			
			if (!internal) { // don't want the cable to return itself
				Optional<IMultipartTile> cable = container.getPartTile(EnumCenterSlot.CENTER);
				if (cable.isPresent() && cable.get() instanceof INetworkConnection) {
					return (INetworkConnection) cable.get();
				}
			}

		} else if (!internal && actualTile instanceof INetworkConnection) {
			return (INetworkConnection) actualTile;
		}
		
		return null;
	}

	@Nullable
	public static INetworkConnection getConnectionType(int networkID, World world, BlockPos pos, EnumFacing dir, boolean internal, boolean cableOnly) {
		TileEntity tile = world.getTileEntity(pos);
		return tile == null ? null : getConnectionType(networkID, tile, dir, internal, cableOnly);
	}

	@Nullable
	public static INetworkConnection getConnectionType(int networkID, TileEntity tile, EnumFacing dir, boolean internal, boolean cableOnly) {
		INetworkConnection networkTile = getNetworkTile(networkID, tile, dir, internal, cableOnly);
		if (networkTile != null && networkTile.canConnect(networkID, dir, internal).canConnect()) {
			return networkTile;
		}
		return null;
	}

	@Nullable
	public static INetworkConnection checkBlockInDirection(ICable cable, EnumFacing dir) {
		if (!cable.canConnect(cable.getRegistryID(), dir, true).canConnect()) {
			return null;
		}
		World actualWorld = getWorldFromCable(cable);
		INetworkConnection internal = getConnectionType(cable.getRegistryID(), actualWorld, cable.getCoords().getBlockPos(), dir, true, false);

		if (internal != null || !cable.canConnect(cable.getRegistryID(), dir, false).canConnect()) {
			return internal;
		}
		return getConnectionType(cable.getRegistryID(), actualWorld, cable.getCoords().getBlockPos().offset(dir), dir.getOpposite(), false, false);

	}

	public static CableRenderType getConnectionRenderType(ICable cable, EnumFacing dir) {
		INetworkConnection connection = checkBlockInDirection(cable, dir);
		return connection == null ? CableRenderType.NONE : connection.getCableRenderSize(dir);
	}

	public static ConnectableType getConnectableType(ICable cable, EnumFacing dir) {
		INetworkConnection connection = CableHelper.checkBlockInDirection(cable, dir);
		return getConnectableType(connection);
	}

	public static ConnectableType getConnectableType(INetworkConnection connection) {
		return connection == null ? ConnectableType.NONE : (connection instanceof IDataCable ? ConnectableType.CONNECTABLE : ConnectableType.TILE);
	}

	/** to see if there is a neighbouring cable to connect to, (only checks external connections) */
	public static Pair<ConnectableType, Integer> getCableConnection(ICable source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		INetworkConnection external = getConnectionType(source.getRegistryID(), getWorldFromCable(source), source.getCoords().getBlockPos().offset(dir), dir.getOpposite(), false, true);
		ConnectableType type = getConnectableType(external);
		return new Pair(type, type == cableType ? ((IDataCable) external).getRegistryID() : -1);
	}

	public static World getWorldFromCable(ICable cable) {
		if (cable instanceof TileEntity) {
			return ((TileEntity) cable).getWorld();
		} else if (cable instanceof TileSonarMultipart) {
			return ((TileSonarMultipart) cable).getPartWorld();
		}
		return null;// oh dear
	}

	public TileDataCable getCable(IBlockAccess world, BlockPos pos) {
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
			if (cable.canConnect(cable.getRegistryID(), face.getOpposite(), false).canConnect()) {
				BlockCoords offset = BlockCoords.translateCoords(cable.getCoords(), face.getOpposite());
				INetworkTile tile = PL2API.getCableHelper().getMultipart(offset, face);
				if (tile instanceof IInfoProvider) {
					logicTiles.add((IInfoProvider) tile);
				}
			}
		}
		return logicTiles;
	}

	public static double[] getPos(IDisplay display, RenderInfoProperties renderInfo) {
		if (display instanceof ConnectedDisplay) {
			ConnectedDisplay connected = (ConnectedDisplay) display;
			if (connected.getTopLeftScreen() != null && connected.getTopLeftScreen().getCoords() != null) {
				BlockPos leftPos = connected.getTopLeftScreen().getCoords().getBlockPos();
				double[] translation = renderInfo.getTranslation();
				switch (display.getCableFace()) {
				case DOWN:
					break;
				case EAST:
					break;
				case NORTH:
					return new double[] { leftPos.getX() - translation[0], leftPos.getY() - translation[1], leftPos.getZ() };
				case SOUTH:
					break;
				case UP:
					break;
				case WEST:
					break;
				default:
					break;
				}
			}
		}
		return new double[] { display.getCoords().getX(), display.getCoords().getY(), display.getCoords().getZ() };
	}

	public static int getSlot(IDisplay display, RenderInfoProperties renderInfo, Vec3d hitVec, int xSize, int ySize) {
		double[] pos = CableHelper.getPos(display, renderInfo);
		int maxH = (int) Math.ceil(renderInfo.getScaling()[0]);
		int minH = 0;
		int maxY = (int) Math.ceil(renderInfo.getScaling()[1]);
		int minY = 0;
		int hSlots = Math.max(1, (Math.round(maxH - minH) * xSize));
		int yPos = (int) ((1 - (hitVec.y - pos[1])) * Math.ceil(display.getDisplayType().height * ySize)), hPos = 0;
		switch (display.getCableFace()) {
		case DOWN:
			switch (display.getRotation()) {
			case EAST:
				hPos = (int) ((maxH - minH - (hitVec.z - pos[2])) * xSize);
				yPos = (int) ((maxH - minH - (hitVec.x - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			case NORTH:
				hPos = (int) ((maxH - minH - (hitVec.x - pos[0])) * xSize);
				yPos = (int) ((minH + (hitVec.z - pos[2])) * 2);
				return ((yPos * hSlots) + hPos);
			case SOUTH:
				hPos = (int) ((minH + (hitVec.x - pos[0])) * xSize);
				yPos = (int) ((maxH - minH - (hitVec.z - pos[2])) * 2);
				return ((yPos * hSlots) + hPos);
			case WEST:
				hPos = (int) ((minH + (hitVec.z - pos[2])) * xSize);
				yPos = (int) ((minH + (hitVec.x - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			default:
				break;
			}
			break;
		case EAST:
			hPos = (int) ((1 + minH - (hitVec.z - pos[2])) * xSize);
			return ((yPos * hSlots) + hPos);
		case NORTH:
			hPos = (int) ((1 - (hitVec.x - pos[0])) * xSize);
			return ((yPos * hSlots) + hPos);
		case SOUTH:
			hPos = (int) ((maxH - minH + (hitVec.x - pos[0])) * xSize);
			return ((yPos * hSlots) + hPos) - maxH * 2;
		case UP:
			switch (display.getRotation()) {
			case EAST:
				hPos = (int) ((maxH - minH - (hitVec.z - pos[2])) * xSize);
				yPos = (int) ((minH + (hitVec.x - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			case NORTH:
				hPos = (int) ((maxH - minH - (hitVec.x - pos[0])) * xSize);
				yPos = (int) ((maxH - (hitVec.z - pos[2])) * 2);
				return ((yPos * hSlots) + hPos);
			case SOUTH:
				hPos = (int) ((maxH - minH + (hitVec.x - pos[0])) * xSize);
				yPos = (int) ((minH + (hitVec.z - pos[2])) * 2);
				return ((yPos * hSlots) + hPos) - maxH * 2;
			case WEST:
				hPos = (int) ((maxH - minH + (hitVec.z - pos[2])) * xSize);
				yPos = (int) ((minH - (hitVec.x - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			default:
				break;
			}

			break;
		case WEST:
			hPos = (int) ((maxH - minH + (hitVec.z - pos[2])) * xSize);
			return ((yPos * hSlots) + hPos) - maxH * 2;
		default:
			break;
		}
		return -1;
	}

	public static int getListSlot(IDisplay display, RenderInfoProperties renderInfo, Vec3d hitVec, double elementSize, double spacing, int maxPageSize) {
		double[] pos = CableHelper.getPos(display, renderInfo);

		int maxH = (int) Math.ceil(renderInfo.getScaling()[0]);
		int minH = 0;
		int maxY = (int) Math.ceil(renderInfo.getScaling()[1]);
		int minY = 0;
		int hSlots = 1;
		double yClick = (1 - (hitVec.y - pos[1])) * Math.ceil(display.getDisplayType().height);

		switch (display.getCableFace()) {
		case DOWN:
			switch (display.getRotation()) {
			case EAST:
				yClick = ((maxH - minH - (hitVec.x - pos[0])) * 2);
				break;
			case NORTH:
				yClick = ((minH + (hitVec.z - pos[2])) * 2);
				break;
			case SOUTH:
				yClick = ((maxH - minH - (hitVec.z - pos[2])) * 2);
				break;
			case WEST:
				yClick = ((minH + (hitVec.x - pos[0])) * 2);
				break;
			default:
				break;
			}
			break;
		case UP:
			switch (display.getRotation()) {
			case EAST:
				yClick = ((minH + (hitVec.x - pos[0])) * 2);
				break;
			case NORTH:
				yClick = ((maxH - (hitVec.z - pos[2])) * 2);
				break;
			case SOUTH:
				yClick = ((minH + (hitVec.z - pos[2])) * 2);
				break;
			case WEST:
				yClick = ((minH - (hitVec.x - pos[0])) * 2);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}

		for (int i = 0; i < maxPageSize; i++) {
			double yStart = (i * elementSize) + (Math.max(0, (i - 1) * spacing)) + 0.0625;
			double yEnd = yStart + elementSize;
			if (yClick > yStart && yClick < yEnd) {
				return i;
			}
		}

		return -1;
	}

	public IDataCable getCableFromCoords(BlockCoords coords) {
		if (coords.getWorld() != null) {
			return getCable(coords.getWorld(), coords.getBlockPos());// FIXME no
																		// need
																		// to
																		// have
																		// two
																		// methods!
		}
		return null;
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
			INetworkConnection connection = checkBlockInDirection(cable, face);

			if (connection != null && !(connection instanceof IDataCable) && validate.isValid(connection)) {
				logicTiles.add((T) connection);
			}

		}
		return logicTiles;
	}

	public ILogisticsNetwork getNetwork(TileEntity tile, EnumFacing dir) {
		// watch out for this null :P
		Pair<ConnectableType, Integer> connection = PL2.getDisplayManager().getConnectionType(null, tile.getWorld(), tile.getPos(), dir, ConnectableType.CONNECTABLE);
		if (connection.a != ConnectableType.NONE && connection.b != -1) {
			ILogisticsNetwork cache = PL2.instance.networkManager.getNetwork(connection.b);
			if (cache != null) {
				return cache;
			}
		}
		return EmptyLogisticsNetwork.INSTANCE;
	}

	public ILogisticsNetwork getNetwork(int registryID) {
		return PL2.instance.networkManager.getNetwork(registryID);
	}

	public static ILogicListenable getMonitorFromIdentity(int identity, boolean isRemote) {
		for (ILogicListenable monitor : Maps.newHashMap(PL2.getInfoManager(isRemote).getMonitors()).values()) {
			if (monitor.getIdentity() != -1 && monitor.getIdentity() == identity) {
				return monitor;
			}
		}
		return null;
	}

	/* public static <T extends ICable> Pair<ConnectableType, Integer> getConnectionType(T source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) { BlockPos offset = pos.offset(dir); IMultipartContainer container = MultipartHelper.getPartContainer(world, offset); if (container != null) { return getConnectionType(source, container, dir, cableType); } else { TileEntity tile = world.getTileEntity(offset); if (tile != null) { return getConnectionTypeFromObject(source, tile, dir, cableType); } } return new Pair(ConnectableType.NONE, -1); } /** checks what cable type can be connected via a certain direction, assumes the other block can connect from this side public static <T extends ICable> Pair<ConnectableType, Integer> getConnectionType(T source, IMultipartContainer container, EnumFacing dir, ConnectableType cableType) { ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(dir.getOpposite())); if (part != null) { return getConnectionTypeFromObject(source, part, dir, cableType); } else { ISlottedPart centre = container.getPartInSlot(PartSlot.CENTER); if (centre != null && centre instanceof IDataCable) { return getConnectionTypeFromObject(source, centre, dir, cableType); } } return new Pair(ConnectableType.NONE, -1); } public static <T extends ICable> Pair<ConnectableType, Integer> getConnectionTypeFromObject(T source, Object connection, EnumFacing dir, ConnectableType cableType) { if (connection instanceof IDataCable) { IDataCable cable = (IDataCable) connection; if (cable.getConnectableType().canConnect(cableType)) { return cable.canConnectOnSide(cable.getRegistryID(), dir.getOpposite(), false) ? new Pair(cable.getConnectableType(), cable.getRegistryID()) : new Pair(ConnectableType.NONE, -1); } } else if (connection instanceof INetworkTile) { return ((INetworkTile) connection).canConnect(dir.getOpposite()).canShowConnection() ? new Pair(ConnectableType.TILE, -1) : new Pair(ConnectableType.NONE, -1); } return new Pair(ConnectableType.NONE, -1); } */
}
