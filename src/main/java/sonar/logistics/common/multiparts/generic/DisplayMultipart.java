package sonar.logistics.common.multiparts.generic;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.INormallyOccludingPart;
import mcmultipart.raytrace.PartMOP;
import mcmultipart.raytrace.RayTraceUtils;
import mcmultipart.raytrace.RayTraceUtils.AdvancedRayTraceResultPart;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockCoords;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.listener.ISonarListenable;
import sonar.core.listener.ListenerTally;
import sonar.core.listener.PlayerListener;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.tiles.cable.NetworkConnectionType;
import sonar.logistics.api.tiles.cable.PL2Properties;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.client.gui.GuiDisplayScreen;
import sonar.logistics.helpers.LogisticsHelper;

public abstract class DisplayMultipart extends LogisticsMultipart implements IByteBufTile, INormallyOccludingPart, IDisplay, IOperatorTile, IFlexibleGui<DisplayMultipart> {

	public SyncTagType.BOOLEAN defaultData = new SyncTagType.BOOLEAN(2); // set default info
	public INetworkReader monitor = null;
	public EnumFacing rotation, face;
	public BlockCoords lastSelected = null;
	public int currentSelected = -1;
	{
		syncList.addPart(defaultData);
	}

	public DisplayMultipart() {
		super();
	}

	public DisplayMultipart(EnumFacing face, EnumFacing rotation) {
		super();
		this.rotation = rotation;
		this.face = face;
	}

	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, PartMOP hit) {
		if (!LogisticsHelper.isPlayerUsingOperator(player)) {
			if (isServer()) {
				if (hit.sideHit != face) {
					openFlexibleGui(player, 0);
				} else {
					return container().onClicked(this, player.isSneaking() ? BlockInteractionType.SHIFT_RIGHT : BlockInteractionType.RIGHT, getWorld(), player, hand, stack, hit);
				}
			}
			return true;
		}
		return false;
	}

	public void update() {
		super.update();
		updateDefaultInfo();
	}

	public void updateDefaultInfo() {
		if (isServer() && !defaultData.getObject()) {
			List<ILogicListenable> monitors = PL2.getServerManager().getViewables(Lists.newArrayList(), this);
			ILogicListenable v;
			if (!monitors.isEmpty() && (v = monitors.get(0)) instanceof IInfoProvider) {
				IInfoProvider monitor = (IInfoProvider) monitors.get(0);
				if (container() != null && monitor != null && monitor.getIdentity() != -1) {
					for (int i = 0; i < Math.min(monitor.getMaxInfo(), maxInfo()); i++) {
						if (container().getInfoUUID(i) == null && container().getDisplayInfo(i).formatList.getObjects().isEmpty())
							container().setUUID(new InfoUUID(monitor.getIdentity(), i), i);
					}
					defaultData.setObject(true);
					sendSyncPacket();
				}
			}
		}
	}

	//// IInfoDisplay \\\\

	public abstract void incrementLayout();

	@Override
	public NetworkConnectionType canConnect(EnumFacing dir) {
		return dir != face ? NetworkConnectionType.NETWORK : NetworkConnectionType.NONE;
	}

	@Override
	public EnumFacing getCableFace() {
		return face;
	}

	public EnumFacing getRotation() {
		return rotation;
	}

	//// EVENTS \\\\

	public void validate() {
		super.validate();
		PL2.getInfoManager(isClient()).addDisplay(this);
		if (isClient()){
			this.requestSyncPacket();
		}
	}

	public void invalidate() {
		super.invalidate();
		PL2.getInfoManager(isClient()).removeDisplay(this);
	}

	//// STATE \\\\

	@Override
	public void addOcclusionBoxes(List<AxisAlignedBB> list) {
		this.addSelectionBoxes(list);
	}

	@Override
	public void harvest(EntityPlayer player, PartMOP hit) {
		if (hit.sideHit == face) {
			container().onClicked(this, player.isSneaking() ? BlockInteractionType.SHIFT_LEFT : BlockInteractionType.LEFT, getWorld(), player, player.getActiveHand(), player.getActiveItemStack(), hit);
			return;
		}
		super.harvest(player, hit);
	}

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(PL2Properties.ORIENTATION, face).withProperty(PL2Properties.ROTATION, rotation);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { PL2Properties.ORIENTATION, PL2Properties.ROTATION });
	}

	//// SAVE \\\\

	@Override
	public NBTTagCompound writeData(NBTTagCompound tag, SyncType type) {
		super.writeData(tag, type);
		tag.setByte("rotation", (byte) rotation.ordinal());
		tag.setByte("face", (byte) face.ordinal());
		return tag;
	}

	@Override
	public void readData(NBTTagCompound tag, SyncType type) {
		super.readData(tag, type);
		rotation = EnumFacing.VALUES[tag.getByte("rotation")];
		face = EnumFacing.VALUES[tag.getByte("face")];
	}

	//// PACKETS \\\\

	public void markChanged(IDirtyPart part) {
		super.markChanged(part);
		List<PlayerListener> viewers = getListenerList().getListeners(ListenerType.INFO);
		for (PlayerListener listener : viewers) {
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, listener.player);
		}
	}

	public void onSyncPacketRequested(EntityPlayer player) {
		super.onSyncPacketRequested(player);
		if (isServer()) {
			getListenerList().addListener(new PlayerListener(player), ListenerType.FULL_INFO);
			PL2.getServerManager().sendViewablesToClientFromScreen(this, player);
		}
	}

	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		buf.writeByte((byte) rotation.ordinal());
		buf.writeByte((byte) face.ordinal());
	}

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			buf.writeInt(currentSelected);
			container().getInfoUUID(currentSelected).writeToBuf(buf);
			break;
		case 1:
			buf.writeInt(currentSelected);
			container().getDisplayInfo(currentSelected).formatList.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			currentSelected = buf.readInt();
			InfoUUID uuid = InfoUUID.getUUID(buf);
			container().setUUID(uuid, currentSelected);
			if (isServer()) {
				PL2.getServerManager().updateViewingMonitors = true;
				this.sendSyncPacket();
			}
			break;
		case 1:
			currentSelected = buf.readInt();
			container().getDisplayInfo(currentSelected).formatList.readFromBuf(buf);
			this.sendSyncPacket();
			break;
		case 2:
			incrementLayout();
			break;
		}
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		rotation = EnumFacing.VALUES[buf.readByte()];
		face = EnumFacing.VALUES[buf.readByte()];
	}

	public PartMOP getPartHit(EntityPlayer player) {
		Vec3d start = RayTraceUtils.getStart(player);
		Vec3d end = RayTraceUtils.getEnd(player);
		AdvancedRayTraceResultPart result = collisionRayTrace(start, end);
		return result == null ? null : result.hit;
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
			PL2.getServerManager().sendViewablesToClientFromScreen(this, player);
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	public String getDisplayName() {
		return PL2Translate.DISPLAY_SCREEN.t();
	}
}
