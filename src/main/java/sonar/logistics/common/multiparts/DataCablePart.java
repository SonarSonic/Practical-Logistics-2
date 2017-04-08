package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.ISlotOccludingPart;
import mcmultipart.multipart.PartSlot;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
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
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipart;
import sonar.core.utils.LabelledAxisAlignedBB;
import sonar.logistics.PL2;
import sonar.logistics.PL2Items;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.cabling.ConnectableType;
import sonar.logistics.api.cabling.IDataCable;
import sonar.logistics.api.cabling.ILogicTile;
import sonar.logistics.api.connecting.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorProvider;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.helpers.CableHelper;

public class DataCablePart extends SonarMultipart implements ISlotOccludingPart, IDataCable, IOperatorTile, IOperatorProvider {

	public static final PropertyEnum<CableRenderType> NORTH = PropertyEnum.<CableRenderType>create("north", CableRenderType.class);
	public static final PropertyEnum<CableRenderType> EAST = PropertyEnum.<CableRenderType>create("east", CableRenderType.class);
	public static final PropertyEnum<CableRenderType> SOUTH = PropertyEnum.<CableRenderType>create("south", CableRenderType.class);
	public static final PropertyEnum<CableRenderType> WEST = PropertyEnum.<CableRenderType>create("west", CableRenderType.class);
	public static final PropertyEnum<CableRenderType> DOWN = PropertyEnum.<CableRenderType>create("down", CableRenderType.class);
	public static final PropertyEnum<CableRenderType> UP = PropertyEnum.<CableRenderType>create("up", CableRenderType.class);

	public int registryID = -1;
	//public boolean connection = false;
	public boolean wasAdded = false;
	public boolean[] isBlocked = new boolean[6];

	public DataCablePart() {
		super();
	}

	public ILogisticsNetwork getNetwork() {
		return LogisticsAPI.getCableHelper().getNetwork(registryID);
	}

	@Override
	public void onConnectionAdded(ILogicTile tile, EnumFacing face) {
		if(!isBlocked(face)){
			getNetwork().addConnection(tile);
		}
	}

	@Override
	public void onConnectionRemoved(ILogicTile tile, EnumFacing face) {
		if(!isBlocked(face)){
			getNetwork().removeConnection(tile);
		}
	}
	
	/*
	@Override
	public void onPartChanged(IMultipart changedPart) {
		if (!this.getWorld().isRemote) {
			boolean wasRemoved = false;
			if(changedPart instanceof IRemovable){
				
			}
			if (!wasRemoved && changedPart instanceof SidedMultipart) {
				SidedMultipart sided = (SidedMultipart) changedPart;
				isBlocked[sided.getFacing().ordinal()] = false;
				this.sendUpdatePacket(true);
			}
			//refreshConnections();
			if (changedPart instanceof LogisticsMultipart) {
				getNetwork().markDirty(RefreshType.FULL);
			}
		}
	}
	*/
	
	//// IDataCable \\\\

	
	
	//@Override
	//public void refreshConnections() {
	//	if (isServer()) {
	//		PL2.getCableManager().refreshConnections(this);
	//	}
	//}

	@Override
	public ConnectableType canRenderConnection(EnumFacing dir) {
		return CableHelper.getConnectionType(this, getContainer().getWorldIn(), getContainer().getPosIn(), dir, getCableType()).a;
	}

	@Override
	public boolean canConnectOnSide(int connectingID, EnumFacing dir) {
		return isBlocked[dir.ordinal()] ? false : getContainer().getPartInSlot(PartSlot.getFaceSlot(dir)) == null;
	}

	public boolean isBlocked(EnumFacing dir) {
		return isBlocked[dir.ordinal()];
	}

	@Override
	public int getRegistryID() {
		return registryID;
	}

	@Override
	public void setRegistryID(int id) {
		if (!this.getWorld().isRemote) {
			registryID = id;
		}
	}

	@Override
	public ConnectableType getCableType() {
		return ConnectableType.CONNECTION;
	}

	public void addToNetwork() {
		if (isServer()) {
			ILogisticsNetwork network = PL2.getCableManager().addCable(this);
			CableHelper.getConnectedTiles(this).forEach(t -> network.addConnection(t));		
		}
	}

	public void removeFromNetwork() {
		if (isServer()) {
			ILogisticsNetwork network = PL2.getNetworkManager().getNetwork(getRegistryID());
			PL2.getCableManager().removeConnection(this.getRegistryID(), this);			
			CableHelper.getConnectedTiles(this).forEach(t -> network.removeConnection(t));					
		}
	}

	//// OPERATOR \\\\

	@Override
	public void updateOperatorInfo() {
		this.requestSyncPacket();
	}

	@Override
	public void addInfo(List<String> info) {
		ItemStack dropStack = getItemStack();
		if (dropStack != null)
			info.add(TextFormatting.UNDERLINE + dropStack.getDisplayName());
		info.add("Network ID: " + registryID);
	}

	@Override
	public boolean performOperation(AdvancedRayTraceResultPart rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (mode == OperatorMode.DEFAULT) {
			List<AxisAlignedBB> bounds = new ArrayList();
			addSelectionBoxes(bounds);
			for (AxisAlignedBB bound : bounds) {
				if (bound instanceof LabelledAxisAlignedBB && bound.equals(rayTrace.bounds)) {
					if (this.isClient()) {
						return true;
					}
					String label = ((LabelledAxisAlignedBB) bound).label;
					EnumFacing face = null;
					face = !label.equals("c") ? EnumFacing.valueOf(label.toUpperCase()) : facing;
					isBlocked[face.ordinal()] = !isBlocked[face.ordinal()];
					IDataCable cable = LogisticsAPI.getCableHelper().getCableFromCoords(BlockCoords.translateCoords(getCoords(), face));
					removeFromNetwork();
					addToNetwork();
					if (cable != null && cable instanceof DataCablePart) {
						DataCablePart part = (DataCablePart) cable;
						part.isBlocked[face.getOpposite().ordinal()] = isBlocked[face.ordinal()];
						part.sendUpdatePacket(true);
						part.markDirty();
						// it's messy, but there is no easier way to check if the cables are connected properly.
						part.removeFromNetwork();
						part.addToNetwork();
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
		ArrayList<AxisAlignedBB> boxes = new ArrayList();
		addSelectionBoxes(boxes);
		boxes.forEach(box -> {
			if (box.intersectsWith(mask)) {
				list.add(box);
			}
		});
	}

	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		list.add(new LabelledAxisAlignedBB(6 * 0.0625, 6 * 0.0625, 6 * 0.0625, 1 - 6 * 0.0625, 1 - 6 * 0.0625, 1 - 6 * 0.0625).labelAxis("c"));
		for (EnumFacing face : EnumFacing.values()) {
			CableRenderType connect = CableHelper.checkBlockInDirection(this, face);
			if (connect.canConnect()) {
				double p = 0.0625;
				double w = (1 - 2 * 0.0625) / 2;
				double heightMin = connect.offsetBounds();
				double heightMax = 6 * 0.0625;
				switch (face) {
				case DOWN:
					list.add(new LabelledAxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w).labelAxis(face.toString()));
					break;
				case EAST:
					list.add(new LabelledAxisAlignedBB(1 - heightMax, w, w, 1 - heightMin, 1 - w, 1 - w).labelAxis(face.toString()));
					break;
				case NORTH:
					list.add(new LabelledAxisAlignedBB(w, w, heightMin, 1 - w, 1 - w, heightMax).labelAxis(face.toString()));
					break;
				case SOUTH:
					list.add(new LabelledAxisAlignedBB(w, w, 1 - heightMax, 1 - w, 1 - w, 1 - heightMin).labelAxis(face.toString()));
					break;
				case UP:
					list.add(new LabelledAxisAlignedBB(w, 1 - heightMax, w, 1 - w, 1 - heightMin, 1 - w).labelAxis(face.toString()));
					break;
				case WEST:
					list.add(new LabelledAxisAlignedBB(heightMin, w, w, heightMax, 1 - w, 1 - w).labelAxis(face.toString()));
					break;
				default:
					list.add(new AxisAlignedBB(w, heightMin, w, 1 - w, heightMax, 1 - w));
					break;
				}
			}
		}

	}
	//// STATE \\\\

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(NORTH, CableHelper.checkBlockInDirection(this, EnumFacing.NORTH)).withProperty(SOUTH, CableHelper.checkBlockInDirection(this, EnumFacing.SOUTH)).withProperty(WEST, CableHelper.checkBlockInDirection(this, EnumFacing.WEST)).withProperty(EAST, CableHelper.checkBlockInDirection(this, EnumFacing.EAST)).withProperty(UP, CableHelper.checkBlockInDirection(this, EnumFacing.UP)).withProperty(DOWN, CableHelper.checkBlockInDirection(this, EnumFacing.DOWN));
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { NORTH, EAST, SOUTH, WEST, DOWN, UP });
	}

	//// EVENTS \\\\

	@Override
	public void onFirstTick() {
		if (!this.getWorld().isRemote && !wasAdded) {
			addToNetwork();
			wasAdded = true;
		}
	}

	@Override
	public void onRemoved() {
		super.onRemoved();
		this.onUnloaded();
	}

	@Override
	public void onUnloaded() {
		if (!this.getWorld().isRemote) {
			this.removeFromNetwork();
			wasAdded = false;
		}
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