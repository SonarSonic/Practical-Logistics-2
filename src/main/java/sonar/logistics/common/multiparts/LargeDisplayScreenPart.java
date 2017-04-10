package sonar.logistics.common.multiparts;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import mcmultipart.MCMultiPartMod;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.api.utils.BlockCoords;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenableList;
import sonar.core.listener.ListenerList;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.BOOLEAN;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.LogisticsAPI;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.operator.OperatorMode;
import sonar.logistics.api.tiles.cable.ConnectableType;
import sonar.logistics.api.tiles.cable.PL2Properties;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayConnections;
import sonar.logistics.api.tiles.displays.DisplayLayout;
import sonar.logistics.api.tiles.displays.DisplayType;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.displays.ILargeDisplay;
import sonar.logistics.client.gui.GuiDisplayScreen;
import sonar.logistics.common.multiparts.generic.DisplayMultipart;
import sonar.logistics.network.PacketConnectedDisplayScreen;

public class LargeDisplayScreenPart extends DisplayMultipart implements ILargeDisplay {

	public int registryID = -1;
	public boolean wasAdded = false;
	public static final PropertyEnum<DisplayConnections> TYPE = PropertyEnum.<DisplayConnections>create("type", DisplayConnections.class);
	public ConnectedDisplay overrideDisplay = null;
	public NBTTagCompound savedTag = null;
	public SyncTagType.BOOLEAN shouldRender = (BOOLEAN) new SyncTagType.BOOLEAN(3); // set default info
	public SyncTagType.BOOLEAN wasLocked = (BOOLEAN) new SyncTagType.BOOLEAN(4);
	public boolean onRenderChange = true;

	{
		syncList.addParts(shouldRender, wasLocked);
	}

	public LargeDisplayScreenPart() {
		super();
	}

	public LargeDisplayScreenPart(EnumFacing dir, EnumFacing rotation) {
		super(dir, rotation);
	}

	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
		if (stack != null && stack.getItem() instanceof IOperatorTool) {
			return false;
		}
		if (isClient()) {
			return true;
		}
		if (hit.sideHit != face) {
			LargeDisplayScreenPart part = (LargeDisplayScreenPart) this.getDisplayScreen().getTopLeftScreen();
			if (part != null) {
				part.openFlexibleGui(player, 0);
			}
			return true;
		}
		return this.container().onClicked(this, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, getWorld(), player, hand, stack, hit);
	}

	@Override
	public boolean performOperation(AdvancedRayTraceResultPart rayTrace, OperatorMode mode, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (getDisplayScreen() != null && !getWorld().isRemote) {
			incrementLayout();
			FontHelper.sendMessage("Screen Layout: " + getDisplayScreen().layout.getObject(), getWorld(), player);
		}
		return false;
	}

	public void update() {
		super.update();
		if (isServer() && onRenderChange) {
			if (this.shouldRender()) {
				this.getDisplayScreen().sendViewers();
			}
			this.sendSyncPacket();
			this.sendByteBufPacket(5);
			onRenderChange = false;
		}
	}

	public void updateDefaultInfo() {
		if (this.getDisplayScreen() != null && this.shouldRender()) {
			super.updateDefaultInfo();
		}
	}

	//// IInfoDisplay \\\\

	@Override
	public InfoContainer container() {
		return getDisplayScreen().container;
	}

	@Override
	public DisplayLayout getLayout() {
		ConnectedDisplay screen = getDisplayScreen();
		return screen == null ? DisplayLayout.ONE : screen.getLayout();
	}

	@Override
	public void incrementLayout() {
		getDisplayScreen().layout.incrementEnum();
		while (!(getDisplayScreen().layout.getObject().maxInfo <= this.maxInfo())) {
			getDisplayScreen().layout.incrementEnum();
		}
		sendSyncPacket();
		getDisplayScreen().sendViewers();
	}

	@Override
	public DisplayType getDisplayType() {
		return DisplayType.LARGE;
	}

	@Override
	public int maxInfo() {
		return 4;
	}

	@Override
	public ConnectableType getConnectableType() {
		return ConnectableType.CONNECTABLE;
	}

	//// ILargeDisplay \\\\

	@Override
	public int getRegistryID() {
		return registryID;
	}

	@Override
	public void setRegistryID(int id) {
		registryID = id;
	}

	@Override
	public ConnectedDisplay getDisplayScreen() {
		return overrideDisplay != null ? overrideDisplay : PL2.getInfoManager(isClient()).getOrCreateDisplayScreen(getWorld(), this, registryID);
	}

	@Override
	public void setConnectedDisplay(ConnectedDisplay connectedDisplay) {
		if (isServer() && shouldRender()) {
			if (this.savedTag != null && !savedTag.hasNoTags()) {
				connectedDisplay.readData(savedTag, SyncType.SAVE);
				savedTag = null;
				connectedDisplay.sendViewers();
				PL2.getServerManager().updateViewingMonitors = true;
			}
		}
	}

	@Override
	public boolean shouldRender() {
		return shouldRender.getObject() && this.getDisplayScreen() != null;
	}

	@Override
	public void setShouldRender(boolean shouldRender) {
		if (shouldRender != this.shouldRender.getObject()) {
			this.shouldRender.setObject(shouldRender);
		}
		onRenderChange = true;
		if (isServer()) {
			PL2.getServerManager().updateViewingMonitors = true;
		}
		this.markDirty();
	}

	//// ILogicViewable \\\\

	public ListenableList<PlayerListener> getListenerList() {
		return getDisplayScreen().getListenerList();
	}

	@Override
	public void onListenerAdded(ListenerTally<PlayerListener> tally) {
		getDisplayScreen().onListenerAdded(tally);
	}

	@Override
	public void onListenerRemoved(ListenerTally<PlayerListener> tally) {
		getDisplayScreen().onListenerRemoved(tally);
	}
	

	//// NETWORK \\\\
	public void onNetworkConnect(ILogisticsNetwork network) {
		super.onNetworkConnect(network);
		getDisplayScreen().setHasChanged();
	}

	public void onNetworkDisconnect(ILogisticsNetwork network) {
		super.onNetworkDisconnect(network);
		getDisplayScreen().setHasChanged();
	}

	@Override
	public boolean canConnectOnSide(int connectingID, EnumFacing dir, boolean internal) {
		return (dir != face && dir != face.getOpposite()) && (connectingID == registryID || !(wasLocked.getObject() || getDisplayScreen().isLocked.getObject()));
	}

	@Override
	public void addInfo(List<String> info) {
		super.addInfo(info);
		info.add("Large Display ID: " + registryID);
		info.add("Should Render " + this.shouldRender.getObject());
	}

	public void addConnection() {
		if (isServer()) {
			PL2.getDisplayManager().addConnection(this);
		}
	}

	public void removeConnection() {
		if (isServer()) {
			PL2.getDisplayManager().removeConnection(this);
		}
	}

	//// EVENTS \\\\
	@Override
	public void validate() {
		super.validate();
		if (isServer() && !wasAdded) {
			addConnection();
			wasAdded = true;
		} else {
			this.requestSyncPacket();
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		wasRemoved = true;
		wasAdded = false;
		this.removeConnection();
	}

	//// MULTIPART \\\\
	public void addSelectionBoxes(List<AxisAlignedBB> list) {
		double p = 0.0625;
		double height = p * 16, width = 0, length = p * 1;

		switch (face) {
		case EAST:
			list.add(new AxisAlignedBB(1, 0, (width) / 2, 1 - length, height, 1 - width / 2));
			break;
		case NORTH:
			list.add(new AxisAlignedBB((width) / 2, 0, length, 1 - width / 2, height, 0));
			break;
		case SOUTH:
			list.add(new AxisAlignedBB((width) / 2, 0, 1, 1 - width / 2, height, 1 - length));
			break;
		case WEST:
			list.add(new AxisAlignedBB(length, 0, (width) / 2, 0, height, 1 - width / 2));
			break;
		case DOWN:
			list.add(new AxisAlignedBB(0, 0, 0, 1, length, 1));
			break;
		case UP:
			list.add(new AxisAlignedBB(0, 1 - 0, 0, 1, 1 - length, 1));
			break;
		default:
			break;

		}
	}

	//// STATE \\\\
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess w, BlockPos pos) {
		IBlockState currentState = state;
		List<EnumFacing> faces = Lists.newArrayList();
		for (EnumFacing face : EnumFacing.VALUES) {
			if (face == this.face || face == this.face.getOpposite()) {
				continue;
			}
			if (this.getWorld() != null) {
				IDisplay display = LogisticsAPI.getCableHelper().getDisplayScreen(BlockCoords.translateCoords(getCoords(), face), this.face);
				if (display != null && display.getDisplayType() == DisplayType.LARGE && ((ILargeDisplay) display).getRegistryID() == registryID) {
					switch (this.face) {
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
		return currentState.withProperty(PL2Properties.ORIENTATION, face).withProperty(PL2Properties.ROTATION, EnumFacing.NORTH).withProperty(TYPE, type);
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { PL2Properties.ORIENTATION, PL2Properties.ROTATION, TYPE });
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	//// SAVE \\\\
	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		if (nbt.hasKey("id")) {
			registryID = nbt.getInteger("id");
			shouldRender.readData(nbt, type);
			wasLocked.readData(nbt, type);
		}
		if (this.isServer() && type.isType(SyncType.SAVE) && nbt.hasKey("connected")) {
			this.savedTag = nbt;
		}
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		if (type.isType(SyncType.DEFAULT_SYNC) || (type.isType(SyncType.SAVE) && this.getDisplayScreen().isLocked.getObject())) {
			nbt.setInteger("id", registryID);
			shouldRender.writeData(nbt, type);
			wasLocked.setObject(this.getDisplayScreen().isLocked.getObject());
			wasLocked.writeData(nbt, type);
		}
		if (this.isServer() && type.isType(SyncType.SAVE) && this.shouldRender()) {
			if (this.getDisplayScreen() != null) {
				this.getDisplayScreen().writeData(nbt, type);
			}
		}
		return super.writeData(nbt, type);
	}

	//// PACKETS \\\\
	public void onSyncPacketRequested(EntityPlayer player) {
		super.onSyncPacketRequested(player);
		ConnectedDisplay screen = this.getDisplayScreen();
		if (screen != null)
			PL2.network.sendTo(new PacketConnectedDisplayScreen(screen, registryID), (EntityPlayerMP) player);
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		super.writePacket(buf, id);
		switch (id) {
		case 5:
			shouldRender.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		super.readPacket(buf, id);
		switch (id) {
		case 5:
			this.shouldRender.readFromBuf(buf);
			break;
		case 6:
			if (getDisplayScreen().isLocked.getObject()) {
				getDisplayScreen().unlock();
			} else {
				getDisplayScreen().lock();
			}
			break;
		}
	}

	//// GUI \\\\
	public Object getServerElement(DisplayMultipart obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(obj) : null;
	}

	public Object getClientElement(DisplayMultipart obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiDisplayScreen(obj) : null;
	}

	@Override
	public void onGuiOpened(DisplayMultipart obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			LargeDisplayScreenPart part = (LargeDisplayScreenPart) this.getDisplayScreen().getTopLeftScreen();
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			PL2.network.sendTo(new PacketConnectedDisplayScreen(this.getDisplayScreen(), registryID), (EntityPlayerMP) player);
			PL2.getServerManager().sendViewablesToClientFromScreen(part, player);
			break;
		}
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.LARGE_DISPLAY_SCREEN;
	}
}
