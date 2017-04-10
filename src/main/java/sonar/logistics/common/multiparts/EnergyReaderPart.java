package sonar.logistics.common.multiparts;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.energy.StoredEnergyStack;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncCoords;
import sonar.core.network.sync.SyncEnergyType;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.SortingDirection;
import sonar.logistics.PL2;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IMonitorInfo;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.readers.EnergyReader;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.utils.InfoUUID;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.gui.GuiEnergyReader;
import sonar.logistics.common.containers.ContainerEnergyReader;
import sonar.logistics.common.multiparts.generic.ReaderMultipart;
import sonar.logistics.connections.handlers.EnergyNetworkHandler;
import sonar.logistics.helpers.EnergyHelper;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredBlockCoords;
import sonar.logistics.info.types.MonitoredEnergyStack;

public class EnergyReaderPart extends ReaderMultipart<MonitoredEnergyStack> implements IByteBufTile {

	public SyncCoords selected = new SyncCoords(1);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 2).addSyncType(SyncType.SPECIAL);
	// public SyncEnum<EnergyReader.SortingType> sortingType = (SyncEnum) new SyncEnum(EnergyReader.SortingType.values(), 3).addSyncType(SyncType.SPECIAL);
	public SyncEnum<EnergyReader.Modes> setting = (SyncEnum) new SyncEnum(EnergyReader.Modes.values(), 3).addSyncType(SyncType.SPECIAL);
	public SyncEnergyType energyType = new SyncEnergyType(4);
	{
		syncList.addParts(selected, sortingOrder, setting, energyType);
	}

	@Override
	public void addHandlerIDs(List<String> ids) {
		ids.add(EnergyNetworkHandler.id);
	}

	//// ILogicReader \\\\

	@Override
	public MonitoredList<MonitoredEnergyStack> sortMonitoredList(MonitoredList<MonitoredEnergyStack> updateInfo, int channelID) {
		EnergyHelper.sortEnergyList(updateInfo, sortingOrder.getObject(), EnergyReader.SortingType.NAME);
		return updateInfo;
	}

	@Override
	public void setMonitoredInfo(MonitoredList<MonitoredEnergyStack> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		IMonitorInfo info = null;
		switch (setting.getObject()) {
		case STORAGE:
			if (selected.getCoords() != null) {
				for (MonitoredEnergyStack stack : updateInfo) {
					if (stack.coords.getMonitoredInfo().syncCoords.getCoords().equals(selected.getCoords())) {
						MonitoredEnergyStack convert = stack.copy();
						convert.energyStack.getObject().convertEnergyType(energyType.getEnergyType());
						info = convert;
						break;
					}
				}
			}
			break;
		case STORAGES:
			info = new LogicInfoList(getIdentity(), MonitoredEnergyStack.id, this.getNetworkID());
			// LogicInfoList list = new LogicInfoList();
			break;
		case TOTAL:
			MonitoredEnergyStack energy = new MonitoredEnergyStack(new StoredEnergyStack(energyType.getEnergyType()), new MonitoredBlockCoords(this.getCoords(), this.getDisplayName()), new StoredItemStack(this.getItemStack()));
			for (MonitoredEnergyStack stack : updateInfo.cloneInfo()) {
				MonitoredEnergyStack convert = stack.copy();
				convert.energyStack.getObject().convertEnergyType(energyType.getEnergyType());
				energy = (MonitoredEnergyStack) energy.joinInfo(convert);
			}
			info = energy;
			break;
		default:
			break;

		}

		/* switch (setting.getObject()) { case FLUID: break; case POS: break; case STORAGE: break; case TANKS: break; default: break; } */
		if (info != null) {
			InfoUUID id = new InfoUUID(getIdentity(), 0);
			IMonitorInfo oldInfo = PL2.getServerManager().info.get(id);
			if (oldInfo == null || !oldInfo.isMatchingType(info) || !oldInfo.isMatchingInfo(info) || !oldInfo.isIdenticalInfo(info)) {
				PL2.getServerManager().changeInfo(id, info);
			}
		}

	}

	//// IChannelledTile \\\\

	@Override
	public ChannelType channelType() {
		return ChannelType.UNLIMITED;
	}

	//// PACKETS \\\\

	@Override
	public void writePacket(ByteBuf buf, int id) {
		switch (id) {
		case ADD:
		case PAIRED:
			selected.writeToBuf(buf);
			return;
		}
		super.writePacket(buf, id);
	}

	@Override
	public void readPacket(ByteBuf buf, int id) {
		switch (id) {
		case ADD:
			selected.readFromBuf(buf);
			return;
		}
		super.readPacket(buf, id);

	}

	//// GUI \\\\

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerEnergyReader(player, this);
		}
		return null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiEnergyReader(player, this);
		}
		return null;
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.ENERGY_READER;
	}

}