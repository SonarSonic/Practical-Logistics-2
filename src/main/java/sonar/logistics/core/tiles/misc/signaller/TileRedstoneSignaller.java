package sonar.logistics.core.tiles.misc.signaller;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.api.core.tiles.connections.EnumCableRenderSize;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.misc.signaller.IRedstoneSignaller;
import sonar.logistics.api.core.tiles.misc.signaller.SignallerModes;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.core.tiles.base.TileSidedLogistics;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TileRedstoneSignaller extends TileSidedLogistics implements IRedstoneSignaller, IByteBufTile, IFlexibleGui {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK, ErrorMessage.NO_STATEMENTS };

	public SyncTagType.BOOLEAN isActive = new SyncTagType.BOOLEAN(1);
	public SyncNBTAbstractList<RedstoneSignallerStatement> statements = new SyncNBTAbstractList(RedstoneSignallerStatement.class, 2);
	public SyncEnum<SignallerModes> mode = new SyncEnum(SignallerModes.values(), 3);
	public static final Function<RedstoneSignallerStatement, Boolean> statement_func = s -> s.wasTrue.getObject();

	{
		syncList.addParts(isActive, statements, mode);
	}

	public void update() {
		super.update();
		if (isClient()) {
			return;
		}
		if (statements.getObjects().isEmpty()) {
			isActive.setObject(false);
			return;
		}
		List<InfoUUID> ids = new ArrayList<>();
		for (RedstoneSignallerStatement statement : statements.getObjects()) {
			statement.addRequiredUUIDs(ids);
		}
		Map<InfoUUID, IInfo> infoList = new HashMap<>();
		for (InfoUUID id : ids) {
			if (!infoList.containsKey(id)) {
				ILogicListenable monitor = ServerInfoHandler.instance().getNetworkTileMap().get(id.getIdentity());
				if (monitor != null && this.network.getGlobalInfoProviders().contains(monitor)) {
					IInfo monitorInfo = ServerInfoHandler.instance().getInfoMap().get(id);
					if (monitorInfo != null)
						infoList.put(id, monitorInfo);
				}
			}
		}
		for (RedstoneSignallerStatement s : statements.getObjects()) {
			boolean matching = s.isMatching(infoList).getBool();
			s.wasTrue.setObject(matching);
		}
		boolean isValid = mode.getObject().checkList(statements.getObjects(), statement_func);
		isActive.setObject(isValid);

	}

	//// Redstone Signaller \\\\

	@Override
	public SyncNBTAbstractList<RedstoneSignallerStatement> getStatements() {
		return statements;
	}

	@Override
	public SyncEnum<SignallerModes> emitterMode() {
		return mode;
	}

	//// REDSTONE \\\\

	//// STATE \\\\

	public boolean isActive() {
		return isActive.getObject();
	}

	//// PACKETS \\\\

	@Override
	public void markChanged(IDirtyPart part) {
		super.markChanged(part);
		if (part == isActive) {
			IBlockState state = world.getBlockState(pos);
			world.notifyNeighborsOfStateChange(pos, blockType, true);
			SonarMultipartHelper.sendMultipartPacketAround(this, 0, 128);
		}
	}

	public void onSyncPacketRequested(EntityPlayer player) {
		super.onSyncPacketRequested(player);
		InfoPacketHelper.sendLocalProviders(this, getIdentity(), player);
	}

	/* @Override public void writeUpdatePacket(PacketBuffer buf) { super.writeUpdatePacket(buf); isActive.writeToBuf(buf); }
	 * @Override public void readUpdatePacket(PacketBuffer buf) { super.readUpdatePacket(buf); isActive.readFromBuf(buf); } */
	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			isActive.writeToBuf(buf);
			break;
		case 1:
			mode.writeToBuf(buf);
			break;
		}
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case 0:
			isActive.readFromBuf(buf);
			if (this.isClient())
				this.markBlockForUpdate();
			break;
		case 1:
			mode.readFromBuf(buf);
			break;
		}
	}

	//// GUI \\\\

	public boolean hasStandardGui() {
		return true;
	}

	@Override
	public void onGuiOpened(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			InfoPacketHelper.sendLocalProviders(this, getIdentity(), player);
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerMultipartSync(this);
		default:
			return null;
		}
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiStatementList(player, this);
		default:
			return null;
		}
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public EnumCableRenderSize getCableRenderSize(EnumFacing dir) {
		return EnumCableRenderSize.NONE;
	}
}
