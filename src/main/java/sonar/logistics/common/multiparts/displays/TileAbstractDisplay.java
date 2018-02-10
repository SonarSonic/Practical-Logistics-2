package sonar.logistics.common.multiparts.displays;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import mcmultipart.RayTraceHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.api.IFlexibleGui;
import sonar.core.api.utils.BlockCoords;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.operator.IOperatorTile;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.client.gui.GuiDisplayScreen;
import sonar.logistics.client.gui.GuiDisplayScreen.GuiState;
import sonar.logistics.common.multiparts.TileSidedLogistics;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.displays.ChunkViewerHandler;
import sonar.logistics.networking.displays.DisplayHelper;
import sonar.logistics.networking.displays.LocalProviderHandler;

public abstract class TileAbstractDisplay extends TileSidedLogistics implements IByteBufTile, IDisplay, IFlexibleGui<TileAbstractDisplay> {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK, TileMessage.NO_READER_SELECTED };

	public SyncTagType.BOOLEAN defaultData = new SyncTagType.BOOLEAN(2); // set default info
	{
		syncList.addPart(defaultData);
	}

	public SyncType getUpdateTagType() {
		return SyncType.SAVE;
	}

	public void update() {
		super.update();
		updateDefaultInfo();
		if (ChunkViewerHandler.instance().hasViewersChanged()) {
			sendInfoContainerPacket();
		}
	}

	public void updateDefaultInfo() {
		if (isServer() && !defaultData.getObject() && networkID.getObject()!=-1) {
			List<ILogicListenable> providers = DisplayHelper.getLocalProvidersFromDisplay(Lists.newArrayList(), world, pos, this);
			ILogicListenable v;
			if (!providers.isEmpty() && (v = providers.get(0)) instanceof IInfoProvider) {
				IInfoProvider monitor = (IInfoProvider) v;
				if (container() != null && monitor != null && monitor.getIdentity() != -1) {
					for (int i = 0; i < Math.min(monitor.getMaxInfo(), container().getMaxCapacity()); i++) {
						if (!InfoUUID.valid(container().getInfoUUID(i)) && container().getDisplayInfo(i).formatList.getObjects().isEmpty()) {
							LocalProviderHandler.doLocalProviderConnect(this, monitor, new InfoUUID(monitor.getIdentity(), i), i);
						}
					}
					sendInfoContainerPacket();
				}
			}
			defaultData.setObject(true);
		}
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		// sendInfoContainerPacket();
		return super.getUpdateTag();
	}

	@Override
	public void sendInfoContainerPacket() {
		List<EntityPlayerMP> players = ChunkViewerHandler.instance().getWatchingPlayers(this);
		players.forEach(listener -> SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) listener));
	}

	//// EVENTS \\\\

	public void onFirstTick() {
		super.onFirstTick();
		PL2.getInfoManager(world.isRemote).addDisplay(this);
	}

	public void invalidate() {
		super.invalidate();
		PL2.getInfoManager(world.isRemote).removeDisplay(this);
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		default:
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case 2:
			container().incrementLayout();
			break;
		}
	}

	public RayTraceResult getPartHit(EntityPlayer player) {
		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(player);
		IBlockState state = world.getBlockState(pos);
		return getBlockType().collisionRayTrace(state, world, pos, vectors.getLeft(), vectors.getRight());
	}

	//// GUI \\\\

	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new ContainerMultipartSync(obj);
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new GuiDisplayScreen(obj, obj.container(), GuiState.values()[id], tag.getInteger("infopos"));
	}

	@Override
	public void onGuiOpened(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		PacketHelper.sendLocalProvidersFromScreen(this, world, pos, player);
		SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
	}

	@Override
	public TileMessage[] getValidMessages() {
		return validStates;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
}
