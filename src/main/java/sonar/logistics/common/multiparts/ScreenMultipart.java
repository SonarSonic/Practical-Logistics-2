package sonar.logistics.common.multiparts;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.Logistics;
import sonar.logistics.api.cabling.NetworkConnectionType;
import sonar.logistics.api.displays.IInfoDisplay;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.operator.IOperatorTool;
import sonar.logistics.api.readers.IInfoProvider;
import sonar.logistics.api.readers.ILogicMonitor;
import sonar.logistics.api.utils.LogisticsHelper;
import sonar.logistics.api.viewers.ViewerTally;
import sonar.logistics.api.viewers.ViewerType;
import sonar.logistics.client.gui.GuiDisplayScreen;

public abstract class ScreenMultipart extends LogisticsMultipart implements IByteBufTile, INormallyOccludingPart, IInfoDisplay, IOperatorTile, IFlexibleGui<ScreenMultipart> {

	public SyncTagType.BOOLEAN defaultData = new SyncTagType.BOOLEAN(2); // set default info
	public ILogicMonitor monitor = null;
	public EnumFacing rotation, face;
	public BlockCoords lastSelected = null;
	public int currentSelected = -1;
	{
		syncList.addPart(defaultData);
	}

	public ScreenMultipart() {
		super();
	}

	public ScreenMultipart(EnumFacing face, EnumFacing rotation) {
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
			ArrayList<IInfoProvider> monitors = Logistics.getServerManager().getLocalMonitors(new ArrayList(), this);
			if (!monitors.isEmpty()) {
				IInfoProvider monitor = monitors.get(0);
				if (container() != null && monitor != null && monitor.getIdentity() != null) {
					for (int i = 0; i < Math.min(monitor.getMaxInfo(), maxInfo()); i++) {
						container().setUUID(new InfoUUID(monitor.getIdentity().hashCode(), i), i);
					}
					defaultData.setObject(true);
					sendSyncPacket();
				}
			}
		}
	}

	//// ILogicViewable \\\\

	@Override
	public void onViewerAdded(EntityPlayer player, List<ViewerTally> type) {
	}

	@Override
	public void onViewerRemoved(EntityPlayer player, List<ViewerTally> type) {
	}

	public UUID getIdentity() {
		return getUUID();
	}

	//// IInfoDisplay \\\\

	public abstract void incrementLayout();

	@Override
	public NetworkConnectionType canConnect(EnumFacing dir) {
		return dir != face ? NetworkConnectionType.NETWORK : NetworkConnectionType.NONE;
	}

	@Override
	public EnumFacing getFace() {
		return face;
	}

	public EnumFacing getRotation() {
		return rotation;
	}

	//// EVENTS \\\\

	public void onFirstTick() {
		super.onFirstTick();
		if (!this.getWorld().isRemote)
			Logistics.getServerManager().addDisplay(this);
		else
			this.requestSyncPacket();
	}

	public void onLoaded() {
		super.onLoaded();
		if (!this.getWorld().isRemote)
			Logistics.getServerManager().addDisplay(this);
	}

	public void onRemoved() {
		super.onRemoved();
		if (!this.getWorld().isRemote)
			Logistics.getServerManager().removeDisplay(this);
	}

	public void onUnloaded() {
		super.onUnloaded();
		if (!this.getWorld().isRemote)
			Logistics.getServerManager().removeDisplay(this);
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
		return state.withProperty(ORIENTATION, face).withProperty(ROTATION, rotation);
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { ORIENTATION, ROTATION });
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
		ArrayList<EntityPlayer> viewers = getViewersList().getViewers(false, ViewerType.INFO);
		for (EntityPlayer player : viewers) {
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
		}
	}

	public void onSyncPacketRequested(EntityPlayer player) {
		super.onSyncPacketRequested(player);
		if (isServer()) {
			this.getViewersList().addViewer(player, ViewerType.FULL_INFO);
			Logistics.getServerManager().sendLocalMonitorsToClientFromScreen(this, player);
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
				Logistics.getServerManager().updateViewingMonitors = true;
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

	public Object getServerElement(ScreenMultipart obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(obj) : null;
	}

	public Object getClientElement(ScreenMultipart obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiDisplayScreen(obj) : null;
	}

	@Override
	public void onGuiOpened(ScreenMultipart obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			Logistics.getServerManager().sendLocalMonitorsToClientFromScreen(this, player);
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	public String getDisplayName() {
		return FontHelper.translate("item.DisplayScreen.name");
	}
}
