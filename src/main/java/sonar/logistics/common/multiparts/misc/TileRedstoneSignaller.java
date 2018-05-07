package sonar.logistics.common.multiparts.misc;

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
import sonar.logistics.api.cabling.CableRenderType;
import sonar.logistics.api.errors.ErrorMessage;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.signaller.EmitterStatement;
import sonar.logistics.api.tiles.signaller.ILogisticsTile;
import sonar.logistics.api.tiles.signaller.SignallerModes;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.client.gui.generic.GuiStatementList;
import sonar.logistics.common.multiparts.TileSidedLogistics;
import sonar.logistics.helpers.PacketHelper;
import sonar.logistics.networking.ServerInfoHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TileRedstoneSignaller extends TileSidedLogistics implements ILogisticsTile, IByteBufTile, IFlexibleGui {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK, ErrorMessage.NO_STATEMENTS };

	public SyncTagType.BOOLEAN isActive = new SyncTagType.BOOLEAN(1);
	public SyncNBTAbstractList<EmitterStatement> statements = new SyncNBTAbstractList(EmitterStatement.class, 2);
	public SyncEnum<SignallerModes> mode = new SyncEnum(SignallerModes.values(), 3);
	public static final Function<EmitterStatement, Boolean> statement_func = s -> s.wasTrue.getObject();

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
		for (EmitterStatement statement : statements.getObjects()) {
			statement.addRequiredUUIDs(ids);
		}
		Map<InfoUUID, IInfo> infoList = new HashMap<>();
		for (InfoUUID id : ids) {
			if (!infoList.containsKey(id)) {
				ILogicListenable monitor = ServerInfoHandler.instance().getIdentityTile(id.getIdentity());
				if (monitor != null && this.network.getGlobalInfoProviders().contains(monitor)) {
					IInfo monitorInfo = ServerInfoHandler.instance().getInfoFromUUID(id);
					if (monitorInfo != null)
						infoList.put(id, monitorInfo);
				}
			}
		}
		for (EmitterStatement s : statements.getObjects()) {
			boolean matching = s.isMatching(infoList).getBool();
			s.wasTrue.setObject(matching);
		}
		boolean isValid = mode.getObject().checkList(statements.getObjects(), statement_func);
		isActive.setObject(isValid);

	}

	//// Redstone Signaller \\\\

	@Override
	public SyncNBTAbstractList<EmitterStatement> getStatements() {
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
		PacketHelper.sendLocalProviders(this, getIdentity(), player);
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
			PacketHelper.sendLocalProviders(this, getIdentity(), player);
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
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return CableRenderType.NONE;
	}
}
