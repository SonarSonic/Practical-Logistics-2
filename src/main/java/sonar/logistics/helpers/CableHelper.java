package sonar.logistics.helpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.IMultipartContainer;
import mcmultipart.multipart.ISlottedPart;
import mcmultipart.multipart.MultipartHelper;
import mcmultipart.multipart.PartSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sonar.core.api.utils.BlockCoords;
import sonar.core.utils.Pair;
import sonar.core.utils.SonarValidation;
import sonar.logistics.Logistics;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IConnectable;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.EmptyNetworkCache;
import sonar.logistics.api.connecting.INetworkCache;
import sonar.logistics.api.displays.ConnectedDisplayScreen;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.displays.ILargeDisplay;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.viewers.ILogicViewable;
import sonar.logistics.api.wrappers.CablingWrapper;
import sonar.logistics.common.multiparts.DataCablePart;
import sonar.logistics.common.multiparts.SidedMultipart;

public class CableHelper extends CablingWrapper {

	public static CableRenderType checkBlockInDirection(DataCablePart cable, EnumFacing dir) {
		IMultipartContainer container = cable.getContainer();
		if (container != null) {
			if (cable.isBlocked[dir.ordinal()]) {
				return CableRenderType.NONE;
			}
			IMultipart part = container.getPartInSlot(PartSlot.getFaceSlot(dir));
			if (part == null)
				part = (IMultipart) LogisticsAPI.getCableHelper().getDisplayScreen(cable.getCoords(), dir);
			if (part != null && part instanceof ILogicTile) {
				if (part instanceof SidedMultipart) {
					SidedMultipart sided = (SidedMultipart) part;
					if (sided.heightMax == 0.0625 * 6) {
						return CableRenderType.NONE;
					} else if (sided.heightMax == 0.0625 * 4) {
						return CableRenderType.HALF;
					}
				}
				ILogicTile tile = (ILogicTile) part;
				if (tile.canConnect(dir.getOpposite()).canShowConnection()) {
					return CableRenderType.INTERNAL;
				}
			}

			Pair<ConnectableType, Integer> connection = CableHelper.getConnectionType(cable, container.getWorldIn(), container.getPosIn(), dir, cable.getCableType());
			return !cable.canConnectOnSide(cable.registryID, dir) || !connection.a.canConnect(cable.getCableType()) ? CableRenderType.NONE : CableRenderType.CABLE;
		}
		return CableRenderType.NONE;
	}

	public static double[] getPos(IInfoDisplay display, RenderInfoProperties renderInfo) {
		if (display instanceof ConnectedDisplayScreen) {
			ConnectedDisplayScreen connected = (ConnectedDisplayScreen) display;
			if (connected.getTopLeftScreen() != null && connected.getTopLeftScreen().getCoords() != null) {
				BlockPos leftPos = connected.getTopLeftScreen().getCoords().getBlockPos();
				double[] translation = renderInfo.getTranslation();
				switch (display.getFace()) {
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

	public static int getSlot(IInfoDisplay display, RenderInfoProperties renderInfo, Vec3d hitVec, int xSize, int ySize) {
		double[] pos = CableHelper.getPos(display, renderInfo);

		int maxH = (int) Math.ceil(renderInfo.getScaling()[0]);
		int minH = 0;
		int maxY = (int) Math.ceil(renderInfo.getScaling()[1]);
		int minY = 0;
		int hSlots = Math.max(1, (Math.round(maxH - minH) * xSize));
		int yPos = (int) ((1 - (hitVec.yCoord - pos[1])) * Math.ceil(display.getDisplayType().height * ySize)), hPos = 0;

		switch (display.getFace()) {
		case DOWN:
			switch (display.getRotation()) {
			case EAST:
				hPos = (int) ((maxH - minH - (hitVec.zCoord - pos[2])) * xSize);
				yPos = (int) ((maxH - minH - (hitVec.xCoord - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			case NORTH:
				hPos = (int) ((maxH - minH - (hitVec.xCoord - pos[0])) * xSize);
				yPos = (int) ((minH + (hitVec.zCoord - pos[2])) * 2);
				return ((yPos * hSlots) + hPos);
			case SOUTH:
				hPos = (int) ((minH + (hitVec.xCoord - pos[0])) * xSize);
				yPos = (int) ((maxH - minH - (hitVec.zCoord - pos[2])) * 2);
				return ((yPos * hSlots) + hPos);
			case WEST:
				hPos = (int) ((minH + (hitVec.zCoord - pos[2])) * xSize);
				yPos = (int) ((minH + (hitVec.xCoord - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			default:
				break;
			}
			break;
		case EAST:
			hPos = (int) ((1 + minH - (hitVec.zCoord - pos[2])) * xSize);
			return ((yPos * hSlots) + hPos);
		case NORTH:
			hPos = (int) ((1 - (hitVec.xCoord - pos[0])) * xSize);
			return ((yPos * hSlots) + hPos);
		case SOUTH:
			hPos = (int) ((maxH - minH + (hitVec.xCoord - pos[0])) * xSize);
			return ((yPos * hSlots) + hPos) - maxH * 2;
		case UP:
			switch (display.getRotation()) {
			case EAST:
				hPos = (int) ((maxH - minH - (hitVec.zCoord - pos[2])) * xSize);
				yPos = (int) ((minH + (hitVec.xCoord - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			case NORTH:
				hPos = (int) ((maxH - minH - (hitVec.xCoord - pos[0])) * xSize);
				yPos = (int) ((maxH - (hitVec.zCoord - pos[2])) * 2);
				return ((yPos * hSlots) + hPos);
			case SOUTH:
				hPos = (int) ((maxH - minH + (hitVec.xCoord - pos[0])) * xSize);
				yPos = (int) ((minH + (hitVec.zCoord - pos[2])) * 2);
				return ((yPos * hSlots) + hPos) - maxH * 2;
			case WEST:
				hPos = (int) ((maxH - minH + (hitVec.zCoord - pos[2])) * xSize);
				yPos = (int) ((minH - (hitVec.xCoord - pos[0])) * 2);
				return ((yPos * hSlots) + hPos);
			default:
				break;
			}

			break;
		case WEST:
			hPos = (int) ((maxH - minH + (hitVec.zCoord - pos[2])) * xSize);
			return ((yPos * hSlots) + hPos) - maxH * 2;
		default:
			break;
		}
		return -1;
	}

	public static int getListSlot(IInfoDisplay display, RenderInfoProperties renderInfo, Vec3d hitVec, double elementSize, double spacing, int maxPageSize) {
		double[] pos = CableHelper.getPos(display, renderInfo);

		int maxH = (int) Math.ceil(renderInfo.getScaling()[0]);
		int minH = 0;
		int maxY = (int) Math.ceil(renderInfo.getScaling()[1]);
		int minY = 0;
		int hSlots = 1;
		double yClick = (1 - (hitVec.yCoord - pos[1])) * Math.ceil(display.getDisplayType().height);

		// int yPos = (int) ((1 - (hitVec.yCoord - pos[1])) * Math.ceil(display.getDisplayType().height * ySize)), hPos = 0;

		switch (display.getFace()) {
		case DOWN:
			switch (display.getRotation()) {
			case EAST:
				yClick = ((maxH - minH - (hitVec.xCoord - pos[0])) * 2);
				break;
			case NORTH:
				yClick = ((minH + (hitVec.zCoord - pos[2])) * 2);
				break;
			case SOUTH:
				yClick = ((maxH - minH - (hitVec.zCoord - pos[2])) * 2);
				break;
			case WEST:
				yClick = ((minH + (hitVec.xCoord - pos[0])) * 2);
				break;
			default:
				break;
			}
			break;
		case UP:
			switch (display.getRotation()) {
			case EAST:
				yClick = ((minH + (hitVec.xCoord - pos[0])) * 2);
				break;
			case NORTH:
				yClick = ((maxH - (hitVec.zCoord - pos[2])) * 2);
				break;
			case SOUTH:
				yClick = ((minH + (hitVec.zCoord - pos[2])) * 2);
				break;
			case WEST:
				yClick = ((minH - (hitVec.xCoord - pos[0])) * 2);
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
				// System.out.println("SLOT:" + i);
				return i;
			}
		}

		return -1;
	}

	public IDataCable getCableFromCoords(BlockCoords coords) {
		if (coords.getWorld() != null) {
			IMultipartContainer container = MultipartHelper.getPartContainer(coords.getWorld(), coords.getBlockPos());
			if (container != null) {
				ISlottedPart part = container.getPartInSlot(PartSlot.CENTER);
				if (part != null && part instanceof IDataCable) {
					return (IDataCable) part;
				}
			}
		}
		return null;
	}

	public ILogicTile getMultipart(BlockCoords coords, EnumFacing face) {
		if (coords.getWorld() != null) {
			IMultipartContainer container = MultipartHelper.getPartContainer(coords.getWorld(), coords.getBlockPos());
			if (container != null) {
				ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(face));
				if (part instanceof ILogicTile) {
					return (ILogicTile) part;
				}
			}
		}
		return null;
	}

	public IInfoDisplay getDisplayScreen(BlockCoords coords, EnumFacing face) {
		if (coords.getWorld() != null) {
			IMultipartContainer container = MultipartHelper.getPartContainer(coords.getWorld(), coords.getBlockPos());
			if (container != null) {
				return getDisplayScreen(container, face);
			}
		}
		return null;
	}

	public ILargeDisplay getDisplayScreen(BlockCoords coords, int registryID) {
		if (coords.getWorld() != null) {
			IMultipartContainer container = MultipartHelper.getPartContainer(coords.getWorld(), coords.getBlockPos());
			if (container != null) {
				return getDisplayScreen(container, registryID);
			}
		}
		return null;
	}

	public IInfoDisplay getDisplayScreen(IMultipartContainer container, EnumFacing face) {
		for (IMultipart part : container.getParts()) {
			if (part != null && part instanceof IInfoDisplay) {
				IInfoDisplay display = (IInfoDisplay) part;
				if (display.getFace() == face) {
					return display;
				}
			}
		}
		return null;
	}

	public ILargeDisplay getDisplayScreen(IMultipartContainer container, int registryID) {
		for (IMultipart part : container.getParts()) {
			if (part != null && part instanceof ILargeDisplay) {
				ILargeDisplay display = (ILargeDisplay) part;
				if (display.getRegistryID() == registryID) {
					return display;
				}
			}
		}
		return null;
	}

	public static ArrayList<ILogicTile> getConnectedTiles(DataCablePart cable) {
		return getConnectedTiles(cable, new SonarValidation.CLASS(ILogicTile.class));
	}

	public static <T> ArrayList<T> getConnectedTiles(DataCablePart cable, Class<T> type) {
		return getConnectedTiles(cable, new SonarValidation.CLASS(type));
	}

	public static ArrayList getConnectedTilesOfTypes(DataCablePart cable, Class... type) {
		return getConnectedTiles(cable, new SonarValidation.CLASSLIST(type));
	}

	public static <T> ArrayList<T> getConnectedTiles(DataCablePart cable, SonarValidation validate) {
		ArrayList<T> logicTiles = new ArrayList();
		for (IMultipart part : cable.getContainer().getParts()) {
			if (validate.isValid(part)) {
				if (part instanceof SidedMultipart) {
					SidedMultipart sided = (SidedMultipart) part;
					if (cable.isBlocked(sided.getFacing())) {
						continue;
					}
				}
				logicTiles.add((T) part);
			}
		}
		for (EnumFacing face : EnumFacing.values()) {
			if (cable.canConnectOnSide(cable.registryID, face)) {
				BlockCoords offset = BlockCoords.translateCoords(cable.getCoords(), face.getOpposite());
				ILogicTile tile = LogisticsAPI.getCableHelper().getMultipart(offset, face);
				if (validate.isValid(tile) && tile.canConnect(face).canConnect()) {
					logicTiles.add((T) tile);
				}
			}
		}
		return logicTiles;
	}

	public static ArrayList<IInfoProvider> getLocalMonitors(DataCablePart cable) {
		ArrayList<IInfoProvider> logicTiles = new ArrayList();
		for (EnumFacing face : EnumFacing.values()) {
			if (!cable.isBlocked(face.getOpposite())) {
				BlockCoords offset = BlockCoords.translateCoords(cable.getCoords(), face.getOpposite());
				ILogicTile tile = LogisticsAPI.getCableHelper().getMultipart(offset, face);
				if (tile instanceof IInfoProvider) {
					logicTiles.add((IInfoProvider) tile);
				}
			}
		}
		return logicTiles;
	}

	public INetworkCache getNetwork(TileEntity tile, EnumFacing dir) {
		// watch out for this null :P
		Pair<ConnectableType, Integer> connection = Logistics.getDisplayManager().getConnectionType(null, tile.getWorld(), tile.getPos(), dir, ConnectableType.CONNECTION);
		if (connection.a != ConnectableType.NONE && connection.b != -1) {
			INetworkCache cache = Logistics.instance.networkManager.getNetwork(connection.b);
			if (cache != null) {
				return cache;
			}
		}
		return EmptyNetworkCache.INSTANCE;
	}

	public INetworkCache getNetwork(int registryID) {
		return Logistics.instance.networkManager.getNetwork(registryID);
	}

	public static ILogicViewable getMonitorFromHashCode(int hashCode, boolean isRemote) {
		for (ILogicViewable monitor : ((LinkedHashMap<UUID, ILogicViewable>) Logistics.getInfoManager(isRemote).getMonitors().clone()).values()) {
			if (monitor.getIdentity() != null && monitor.getIdentity().hashCode() == hashCode) {
				return monitor;
			}
		}
		return null;
	}

	public static <T extends IConnectable> Pair<ConnectableType, Integer> getConnectionType(T source, World world, BlockPos pos, EnumFacing dir, ConnectableType cableType) {
		BlockPos offset = pos.offset(dir);
		IMultipartContainer container = MultipartHelper.getPartContainer(world, offset);
		if (container != null) {
			return getConnectionType(source, container, dir, cableType);
		} else {
			TileEntity tile = world.getTileEntity(offset);
			if (tile != null) {
				return getConnectionTypeFromObject(source, tile, dir, cableType);
			}
		}
		return new Pair(ConnectableType.NONE, -1);
	}

	/** checks what cable type can be connected via a certain direction, assumes the other block can connect from this side */
	public static <T extends IConnectable> Pair<ConnectableType, Integer> getConnectionType(T source, IMultipartContainer container, EnumFacing dir, ConnectableType cableType) {
		ISlottedPart part = container.getPartInSlot(PartSlot.getFaceSlot(dir.getOpposite()));
		if (part != null) {
			return getConnectionTypeFromObject(source, part, dir, cableType);
		} else {
			ISlottedPart centre = container.getPartInSlot(PartSlot.CENTER);
			if (centre != null && centre instanceof IDataCable) {
				return getConnectionTypeFromObject(source, centre, dir, cableType);
			}
		}
		return new Pair(ConnectableType.NONE, -1);
	}

	public static <T extends IConnectable> Pair<ConnectableType, Integer> getConnectionTypeFromObject(T source, Object connection, EnumFacing dir, ConnectableType cableType) {
		if (connection instanceof IDataCable) {
			IDataCable cable = (IDataCable) connection;
			if (cable.getCableType().canConnect(cableType)) {
				return cable.canConnectOnSide(cable.getRegistryID(), dir.getOpposite()) ? new Pair(cable.getCableType(), cable.getRegistryID()) : new Pair(ConnectableType.NONE, -1);
			}
		} else if (connection instanceof ILogicTile) {
			return ((ILogicTile) connection).canConnect(dir.getOpposite()).canShowConnection() ? new Pair(ConnectableType.BLOCK_CONNECTION, -1) : new Pair(ConnectableType.NONE, -1);
		}
		return new Pair(ConnectableType.NONE, -1);
	}
}
