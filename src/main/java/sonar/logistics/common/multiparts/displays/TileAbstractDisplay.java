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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
import sonar.logistics.common.multiparts.TileSidedLogistics;
import sonar.logistics.helpers.LogisticsHelper;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.displays.ChunkViewerHandler;

public abstract class TileAbstractDisplay extends TileSidedLogistics implements IByteBufTile, IDisplay, IOperatorTile, IFlexibleGui<TileAbstractDisplay> {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK, TileMessage.NO_READER_SELECTED };

	public SyncTagType.BOOLEAN defaultData = new SyncTagType.BOOLEAN(2); // set default info
	public INetworkReader monitor = null;
	public EnumFacing rotation = EnumFacing.NORTH; // FIXME - when it's placed set the rotation
	public BlockCoords lastSelected = null;
	public int currentSelected = -1;
	{
		syncList.addPart(defaultData);
	}

	public SyncType getUpdateTagType() {
		return SyncType.SAVE;
	}

	public void update() {
		super.update();
		updateDefaultInfo();
	}

	public boolean isDoubleClick() {
		return false;
	}

	public void updateDefaultInfo() {
		if (isServer() && !defaultData.getObject()) {
			List<ILogicListenable> providers = LogisticsHelper.getLocalProviders(Lists.newArrayList(), world, pos, this);
			ILogicListenable v;
			if (!providers.isEmpty() && (v = providers.get(0)) instanceof IInfoProvider) {
				IInfoProvider monitor = (IInfoProvider) v;
				if (container() != null && monitor != null && monitor.getIdentity() != -1) {

					for (int i = 0; i < Math.min(monitor.getMaxInfo(), maxInfo()); i++) {
						if (container().getInfoUUID(i) == null && container().getDisplayInfo(i).formatList.getObjects().isEmpty()) {
							container().setUUID(new InfoUUID(monitor.getIdentity(), i), i);
						}
					}
					defaultData.setObject(true);
					sendSyncPacket();
					List<EntityPlayerMP> players = ChunkViewerHandler.instance().getWatchingPlayers(this);
					players.forEach(player -> {
						PacketHelper.sendLocalProvidersFromScreen(this, world, pos, player);
						SonarMultipartHelper.sendMultipartSyncToPlayer(this, player);
					});
				}
			}
		}
	}

	//// IInfoDisplay \\\\

	public abstract void incrementLayout();

	public EnumFacing getRotation() {
		return rotation;
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

	@Override
	public NBTTagCompound writeData(NBTTagCompound tag, SyncType type) {
		super.writeData(tag, type);
		tag.setByte("rotation", (byte) rotation.ordinal());
		return tag;
	}

	@Override
	public void readData(NBTTagCompound tag, SyncType type) {
		super.readData(tag, type);
		rotation = EnumFacing.VALUES[tag.getByte("rotation")];
	}

	//// PACKETS \\\\

	public void markChanged(IDirtyPart part) {
		super.markChanged(part);
		if (isServer()) {
			List<EntityPlayerMP> players = ChunkViewerHandler.instance().getWatchingPlayers(this);
			for (EntityPlayerMP player : players) {
				SonarMultipartHelper.sendMultipartSyncToPlayer(this, player);
			}
		}
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
				PL2.getServerManager().updateListenerDisplays = true;
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

	public RayTraceResult getPartHit(EntityPlayer player) {

		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(player);
		IBlockState state = world.getBlockState(pos);
		return getBlockType().collisionRayTrace(state, world, pos, vectors.getLeft(), vectors.getRight());
	}

	//// GUI \\\\

	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new ContainerMultipartSync(obj) : null;
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return id == 0 ? new GuiDisplayScreen(obj) : null;
	}

	@Override
	public void onGuiOpened(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			PacketHelper.sendLocalProvidersFromScreen(this, world, pos, player);
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	@Override
	public TileMessage[] getValidMessages() {
		return validStates;
	}
}
