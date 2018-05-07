package sonar.logistics.common.multiparts.cables;

import mcmultipart.api.slot.EnumFaceSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RayTraceHelper;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.integration.multipart.TileSonarMultipart;
import sonar.core.network.sync.SyncTagType;
import sonar.core.utils.LabelledAxisAlignedBB;
import sonar.core.utils.Pair;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.cabling.CableConnectionType;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.networking.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.api.utils.PL2RemovalType;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.cabling.CableConnectionHandler;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.networking.events.LogisticsEventHandler;

import java.util.ArrayList;
import java.util.List;

public class TileDataCable extends TileSonarMultipart implements IDataCable, IOperatorTile, IOperatorProvider {

	public int[] isBlocked = new int[6];
	public int[] isConnected = new int[6];
	public SyncTagType.INT registryID = (SyncTagType.INT) new SyncTagType.INT(0).setDefault(-1);

	{this.syncList.addPart(registryID);}

	public ILogisticsNetwork getNetwork() {
		return LogisticsNetworkHandler.instance().getNetwork(registryID.getObject());
	}
	
	public final void onFirstTick(){
		super.onFirstTick();
		LogisticsEventHandler.instance().queueNetworkAddition(this, PL2AdditionType.PLAYER_ADDED);
	}

	public final void invalidate() {
		super.invalidate();
		LogisticsEventHandler.instance().queueNetworkRemoval(this, PL2RemovalType.PLAYER_REMOVED);
	}
	
	@Override
	public void updateCableRenders() {
		if (isServer()) {
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

	public void invertBlock(EnumFacing dir) {
		isBlocked[dir.ordinal()] = isBlocked[dir.ordinal()] == 1 ? 0 : 1;
	}

	public boolean isInternallyBlocked(EnumFacing dir) {
		return info != null && dir != null && info.getContainer().getPart(EnumFaceSlot.fromFace(dir)).isPresent();
	}

	@Override
	public CableConnectionType canConnect(int registryID, ConnectableType type, EnumFacing dir, boolean internal) {
		if (type != this.getConnectableType()) {
			return CableConnectionType.NONE;
		}
		boolean internallyBlocked = isInternallyBlocked(dir);
		if (isBlocked(dir) || (!internal && internallyBlocked)) {
			return CableConnectionType.NONE;
		}
		return CableConnectionType.NETWORK;
	}

	@Override
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.CABLE;
	}

	@Override
	public int getRegistryID() {
		return registryID.getObject();
	}

	@Override
	public void setRegistryID(int id) {
		if(registryID.getObject() != id) {
			registryID.setObject(id);
		}
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
		info.add("Network ID: " + registryID.getObject());
	}

	@Override
	public boolean performOperation(RayTraceResult rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (mode == OperatorMode.DEFAULT) {
			Pair<Vec3d, Vec3d> look = RayTraceHelper.getPlayerLookVec(player);
			Pair<RayTraceResult, AxisAlignedBB> trace = RayTraceHelper.rayTraceBoxes(pos, look.getLeft(), look.getRight(), BlockDataCable.getSelectionBoxes(world, pos, new ArrayList<>()));

			if (trace != null && trace.b instanceof LabelledAxisAlignedBB) {
				if (isClient()) {
					return true;
				}
				String label = ((LabelledAxisAlignedBB) trace.b).label;
				EnumFacing face = null;
				face = !label.equals("c") ? EnumFacing.valueOf(label.toUpperCase()) : facing;
				TileDataCable adjCable = CableHelper.getCable(world, pos.offset(face));
				if (adjCable != null) {

					//remove both cables
					CableConnectionHandler.instance().removeConnection(this);
					CableConnectionHandler.instance().removeConnection(adjCable);
					
					//change blocked settings						
					invertBlock(face);					
					adjCable.isBlocked[face.getOpposite().ordinal()] = isBlocked[face.ordinal()];					
					
					//add both cables back again
					CableConnectionHandler.instance().addConnection(adjCable);
					CableConnectionHandler.instance().addConnection(this);
					
					//update networking

					ILogisticsNetwork thisNet = LogisticsNetworkHandler.instance().getOrCreateNetwork(getRegistryID());
					ILogisticsNetwork adjNetNet = LogisticsNetworkHandler.instance().getOrCreateNetwork(adjCable.getRegistryID());
					thisNet.onCablesChanged();
					adjNetNet.onCablesChanged();
					
					//update cable render
					SonarMultipartHelper.sendMultipartUpdateSyncAround(this, 128);
					SonarMultipartHelper.sendMultipartUpdateSyncAround(adjCable, 128);
				}
				return true;

			}
		}
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