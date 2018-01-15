package sonar.logistics.common.multiparts.cables;

import java.util.List;

import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.networking.connections.CableConnectionHandler;

public class TileDataCable extends TileSonarMultipart implements IDataCable, IOperatorTile, IOperatorProvider {

	public int[] isBlocked = new int[6];
	public int[] isConnected = new int[6];

	public int registryID = -1;

	public ILogisticsNetwork getNetwork() {
		return PL2.instance.getNetworkManager().getNetwork(registryID);
	}

	@Override
	public void onFirstTick() {
		super.onFirstTick();
		if (isServer()) {
			CableConnectionHandler.instance().queueCableAddition(this);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (isServer()) {
			CableConnectionHandler.instance().queueCableRemoval(this);
		}
	}

	@Override
	public void updateCableRenders() {
		if (isServer()) {
			for (EnumFacing face : EnumFacing.values()) {
				isConnected[face.ordinal()] = CableHelper.getConnectionRenderType(this, face).ordinal();
			}
			SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
		}
	}

	public CableRenderType getRenderType(EnumFacing face) {
		return CableRenderType.values()[isConnected[face.ordinal()]];
	}

	@Override
	public ConnectableType canRenderConnection(EnumFacing dir) {
		return CableHelper.getConnectableType(this, dir);
	}

	public boolean isBlocked(EnumFacing dir) {
		return isBlocked[dir.ordinal()] == 1;
	}

	public boolean isInternallyBlocked(EnumFacing dir) {
		return info == null || dir == null ? false : info.getContainer().getPart(EnumFaceSlot.fromFace(dir)).isPresent();
	}

	@Override
	public NetworkConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		boolean internallyBlocked = isInternallyBlocked(dir);
		if (isBlocked(dir) || (!internal && internallyBlocked)) {
			return NetworkConnectionType.NONE;
		}
		return NetworkConnectionType.NETWORK;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.CABLE;
	}

	@Override
	public int getRegistryID() {
		return registryID;
	}

	@Override
	public void setRegistryID(int id) {
		registryID = id;
	}

	@Override
	public ConnectableType getConnectableType() {
		return ConnectableType.CONNECTABLE;
	}

	//// OPERATOR \\\\

	@Override
	public void updateOperatorInfo() {
		requestSyncPacket();
	}

	@Override
	public void addInfo(List<String> info) {
		info.add(TextFormatting.UNDERLINE + PL2Multiparts.DATA_CABLE.getDisplayName());
		info.add("Network ID: " + registryID);
	}

	@Override
	public boolean performOperation(RayTraceResult rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		/* if (mode == OperatorMode.DEFAULT) { List<AxisAlignedBB> bounds = Lists.newArrayList(); addSelectionBoxes(bounds); for (AxisAlignedBB bound : bounds) { if (bound instanceof LabelledAxisAlignedBB && bound.equals(rayTrace.bounds)) { if (isClient()) { return true; } String label = ((LabelledAxisAlignedBB) bound).label; EnumFacing face = null; face = !label.equals("c") ? EnumFacing.valueOf(label.toUpperCase()) : facing; isBlocked[face.ordinal()] = !isBlocked[face.ordinal()]; IDataCable cable = PL2API.getCableHelper().getCableFromCoords(BlockCoords. translateCoords(getCoords(), face)); removeConnection(); addConnection(); if (cable != null && cable instanceof DataCablePart) { DataCablePart part = (DataCablePart) cable; part.isBlocked[face.getOpposite().ordinal()] = isBlocked[face.ordinal()]; part.sendUpdatePacket(true); part.markDirty(); // it's messy, but there is no easier way to check if the cables are connected properly. part.removeConnection(); part.addConnection(); } sendUpdatePacket(true); markDirty(); return true; } } } */
		return false;
	}

	//// SAVING \\\\

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		isBlocked = nbt.getIntArray("isBlocked");
		isConnected = nbt.getIntArray("isConnected");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		nbt.setIntArray("isBlocked", isBlocked);
		nbt.setIntArray("isConnected", isConnected);
		return super.writeData(nbt, type);
	}
}