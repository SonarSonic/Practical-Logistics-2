package sonar.logistics.common.multiparts.misc;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import mcmultipart.MCMultiPartMod;
import mcmultipart.multipart.IRedstonePart;
import mcmultipart.raytrace.PartMOP;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.integration.multipart.SonarMultipartHelper;
import sonar.core.network.sync.IDirtyPart;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.PL2Properties;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.api.tiles.signaller.EmitterStatement;
import sonar.logistics.api.tiles.signaller.ILogisticsTile;
import sonar.logistics.api.tiles.signaller.SignallerModes;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.client.gui.generic.GuiStatementList;
import sonar.logistics.common.containers.ContainerStatementList;
import sonar.logistics.common.multiparts.SidedPart;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.LogisticsHelper;

public class RedstoneSignallerPart extends SidedPart implements IRedstonePart, ILogisticsTile, IByteBufTile, IFlexibleGui {

	public static final TileMessage[] validStates = new TileMessage[] { TileMessage.NO_NETWORK, TileMessage.NO_STATEMENTS};

	public static final PropertyBool ACTIVE = PropertyBool.create("active");
	public SyncTagType.BOOLEAN isActive = new SyncTagType.BOOLEAN(1);
	public SyncNBTAbstractList<EmitterStatement> statements = new SyncNBTAbstractList(EmitterStatement.class, 2);
	public SyncEnum<SignallerModes> mode = new SyncEnum(SignallerModes.values(), 3);
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
		List<InfoUUID> ids = Lists.newArrayList();
		for (EmitterStatement statement : statements.getObjects()) {
			statement.addRequiredUUIDs(ids);
		}
		Map<InfoUUID, IInfo> infoList = Maps.newHashMap();
		for (InfoUUID id : ids) {
			if (!infoList.containsKey(id)) {
				ILogicListenable monitor = CableHelper.getMonitorFromIdentity(id.getIdentity(), false);
				if (monitor != null && this.network.getLocalInfoProviders().contains(monitor)) {
					IInfo monitorInfo = PL2.getServerManager().getInfoFromUUID(id);
					if (monitorInfo != null)
						infoList.put(id, monitorInfo);
				}
			}
		}
		switch (mode.getObject()) {
		case ALL_FALSE:
			for (EmitterStatement statement : statements.getObjects()) {
				boolean matching = statement.isMatching(infoList).getBool();
				statement.wasTrue.setObject(matching);
				if (matching) {
					isActive.setObject(false);
					return;
				}
			}
			isActive.setObject(true);
			break;
		case ALL_TRUE:
			for (EmitterStatement statement : statements.getObjects()) {
				boolean matching = statement.isMatching(infoList).getBool();
				statement.wasTrue.setObject(matching);
				if (!matching) {
					isActive.setObject(false);
					return;
				}
			}
			isActive.setObject(true);
			break;
		case ONE_FALSE:
			for (EmitterStatement statement : statements.getObjects()) {
				boolean matching = statement.isMatching(infoList).getBool();
				statement.wasTrue.setObject(matching);
				if (!matching) {
					isActive.setObject(true);
					return;
				}
			}
			isActive.setObject(false);
			break;
		case ONE_TRUE:
			for (EmitterStatement statement : statements.getObjects()) {
				boolean matching = statement.isMatching(infoList).getBool();
				statement.wasTrue.setObject(matching);
				if (matching) {
					isActive.setObject(true);
					return;
				}
			}
			isActive.setObject(false);
			break;
		default:
			break;

		}

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
	
	@Override
	public boolean canConnectRedstone(EnumFacing side) {
		return true;
	}

	@Override
	public int getWeakSignal(EnumFacing side) {
		return (isActive()) ? 15 : 0;
	}

	@Override
	public int getStrongSignal(EnumFacing side) {
		return (isActive()) ? 15 : 0;
	}
	
	//// STATE \\\\

	public boolean isActive() {
		return isActive.getObject();
	}

	@Override
	public IBlockState getActualState(IBlockState state) {
		return state.withProperty(PL2Properties.ORIENTATION, getCableFace()).withProperty(ACTIVE, isActive());
	}

	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(MCMultiPartMod.multipart, new IProperty[] { PL2Properties.ORIENTATION, ACTIVE });
	}	
	
	//// PACKETS \\\\

	@Override
	public void markChanged(IDirtyPart part) {
		super.markChanged(part);
		if (part == isActive && this.getWorld() != null) {
			SonarMultipartHelper.sendMultipartPacketAround(this, 0, 128);
			this.notifyBlockUpdate();
		}
	}
	
	public void onSyncPacketRequested(EntityPlayer player) {
		super.onSyncPacketRequested(player);
		PL2.getServerManager().sendViewablesToClient(this, getIdentity(), player);
	}
	
	@Override
	public void writeUpdatePacket(PacketBuffer buf) {
		super.writeUpdatePacket(buf);
		isActive.writeToBuf(buf);
	}

	@Override
	public void readUpdatePacket(PacketBuffer buf) {
		super.readUpdatePacket(buf);
		isActive.readFromBuf(buf);
	}

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
				this.markRenderUpdate();
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
			PL2.getServerManager().sendViewablesToClient(this, getIdentity(), player);
			SonarMultipartHelper.sendMultipartSyncToPlayer(this, (EntityPlayerMP) player);
			break;
		}
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerStatementList(player, this);
		}
		return null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiStatementList(player, this);
		}
		return null;
	}

	@Override
	public TileMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.REDSTONE_SIGNALLER;
	}
}
