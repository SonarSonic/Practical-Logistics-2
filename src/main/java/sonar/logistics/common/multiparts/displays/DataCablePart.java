package sonar.logistics.common.multiparts.displays;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.ISlotOccludingPart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.utils.LabelledAxisAlignedBB;
import sonar.logistics.PL2;
import sonar.logistics.PL2Items;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2API;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.PL2Properties.PropertyCableFace;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.INetworkTile;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.IDataCable;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.helpers.CableHelper;

public class DataCablePart extends SonarMultipart implements ISlotOccludingPart, IDataCable, IOperatorTile, IOperatorProvider {

	public boolean[] isBlocked = new boolean[6];
	public int registryID = -1;

	public ILogisticsNetwork getNetwork() {
		return PL2API.getCableHelper().getNetwork(registryID);
	}

	@Override
	public void validate() {
		if (isServer())
			addConnection();
	}

	@Override
	public void invalidate() {
		if (isServer())
			removeConnection();
	}

	public void addConnection() {
	}

	public void removeConnection() {
	}

	@Override
	public void onLocalProviderAdded(IInfoProvider tile, EnumFacing face) {
		if (canConnectOnSide(tile.getNetworkID(), face, false))
			getNetwork().addLocalInfoProvider(tile);
	}

	@Override
	public void onLocalProviderRemoved(IInfoProvider tile, EnumFacing face) {
		if (canConnectOnSide(tile.getNetworkID(), face, false))
			getNetwork().removeLocalInfoProvider(tile);
	}


	@Override
	public void onNeighborTileChange(EnumFacing facing) {
		super.onNeighborTileChange(facing);
		if (isServer()) {
			ILogisticsNetwork network = getNetwork();
			CableHelper.getLocalMonitors(this).forEach(m -> network.addLocalInfoProvider(m));
		}
	}

	@Override
	public ConnectableType canRenderConnection(EnumFacing dir) {
		return CableHelper.getCableConnection(this, getContainer().getWorldIn(), getContainer().getPosIn(), dir, getConnectableType()).a;
	}

	@Override
	public boolean canConnectOnSide(int connectingID, EnumFacing dir, boolean internal) {
		return !isBlocked[dir.ordinal()] ? (internal ? true : getContainer().getPartInSlot(PartSlot.getFaceSlot(dir)) == null) : false;
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
		if (mode == OperatorMode.DEFAULT) {
			List<AxisAlignedBB> bounds = Lists.newArrayList();
			addSelectionBoxes(bounds);
			for (AxisAlignedBB bound : bounds) {
				if (bound instanceof LabelledAxisAlignedBB && bound.equals(rayTrace.bounds)) {
					if (isClient()) {
						return true;
					}
					String label = ((LabelledAxisAlignedBB) bound).label;
					EnumFacing face = null;
					face = !label.equals("c") ? EnumFacing.valueOf(label.toUpperCase()) : facing;
					isBlocked[face.ordinal()] = !isBlocked[face.ordinal()];
					IDataCable cable = PL2API.getCableHelper().getCableFromCoords(BlockCoords.translateCoords(getCoords(), face));
					removeConnection();
					addConnection();
					if (cable != null && cable instanceof DataCablePart) {
						DataCablePart part = (DataCablePart) cable;
						part.isBlocked[face.getOpposite().ordinal()] = isBlocked[face.ordinal()];
						part.sendUpdatePacket(true);
						part.markDirty();
						// it's messy, but there is no easier way to check if the cables are connected properly.
						part.removeConnection();
						part.addConnection();
					}
					sendUpdatePacket(true);
					markDirty();
					return true;
				}
			}
		}
		return false;
	}

	//// MULTIPART \\\\

	@Override
	public EnumSet<PartSlot> getSlotMask() {
		return EnumSet.of(PartSlot.CENTER);
	}

	@Override
	public EnumSet<PartSlot> getOccludedSlots() {
		EnumSet set = EnumSet.noneOf(PartSlot.class);
		for (PartSlot slot : PartSlot.FACES) {
			EnumFacing face = slot.f1;
			if (CableHelper.checkBlockInDirection(this, face).canConnect()) {
				set.add(slot);
			}
		}
		return set;
	}

	@Override
	public void addCollisionBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		List<AxisAlignedBB> boxes = Lists.newArrayList();
		addSelectionBoxes(boxes);
		boxes.forEach(box -> {
			if (box.intersectsWith(mask)) {
				list.add(box);
			}
		});
	}

	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		double p = 0.0625;
		list.add(new LabelledAxisAlignedBB(6 * p, 6 * p, 6 * p, 1 - 6 * p, 1 - 6 * p, 1 - 6 * p).labelAxis("c"));
		for (EnumFacing face : EnumFacing.values()) {
			CableRenderType connect = CableHelper.checkBlockInDirection(this, face);
			if (connect.canConnect()) {
				list.add(PL2Properties.getCableBox(connect, face));
			}
		}
	}

	//// STATE \\\\
	@Override
	public IBlockState getActualState(IBlockState state) {
		for (PropertyCableFace p : PL2Properties.PROPS) {
			state = state.withProperty(p, CableHelper.checkBlockInDirection(this, p.face));
		}
		return state;
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, PL2Properties.PROPS);
	}

	//// SAVING \\\\

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		isBlocked = new boolean[6];
		NBTTagCompound tag = nbt.getCompoundTag("isBlocked");
		for (int i = 0; i < isBlocked.length; i++) {
			isBlocked[i] = tag.getBoolean("" + i);
		}
		if (type.isType(SyncType.DEFAULT_SYNC)) {
			registryID = nbt.getInteger("id");
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		NBTTagCompound tag = new NBTTagCompound();
		for (int i = 0; i < isBlocked.length; i++) {
			tag.setBoolean("" + i, isBlocked[i]);
		}
		nbt.setTag("isBlocked", tag);
		if (type.isType(SyncType.DEFAULT_SYNC)) {
			nbt.setInteger("id", registryID);
		}
		return super.writeData(nbt, type);
	}

	//// PACKETS \\\\

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		for (int i = 0; i < isBlocked.length; i++) {
			buf.writeBoolean(isBlocked[i]);
		}
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		for (int i = 0; i < isBlocked.length; i++) {
			isBlocked[i] = buf.readBoolean();
		}
	}

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(PL2Items.cable);
	}

	public boolean equals(Object obj) {
		if (obj instanceof DataCablePart) {
			return ((DataCablePart) obj).getCoords().equals(this.getCoords());
		}
		return true;
	}

}