package sonar.logistics.core.tiles.readers.energy;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import sonar.logistics.PL2Blocks;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.displays.info.lists.IMonitoredValue;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.ChannelType;
import sonar.logistics.base.channels.NodeConnection;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.core.tiles.displays.info.types.LogicInfoList;
import sonar.logistics.core.tiles.displays.info.types.channels.MonitoredBlockCoords;
import sonar.logistics.core.tiles.displays.info.types.energy.MonitoredEnergyStack;
import sonar.logistics.core.tiles.readers.base.TileAbstractListReader;
import sonar.logistics.core.tiles.readers.energy.EnergyReader.SortingType;
import sonar.logistics.core.tiles.readers.energy.handling.EnergyNetworkHandler;

import java.util.List;

public class TileEnergyReader extends TileAbstractListReader<MonitoredEnergyStack> implements IByteBufTile {

	public static final ErrorMessage[] validStates = new ErrorMessage[] { ErrorMessage.NO_NETWORK };

	public SyncCoords selected = new SyncCoords(1);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 2).addSyncType(SyncType.SPECIAL);
	public SyncEnum<EnergyReader.Modes> setting = (SyncEnum) new SyncEnum(EnergyReader.Modes.values(), 3).addSyncType(SyncType.SPECIAL);
	public SyncEnergyType energyType = new SyncEnergyType(4);
	public EnergySorter energy_sorter = new EnergySorter() {

		public SortingDirection getDirection() {
			return sortingOrder.getObject();
		}

		public SortingType getType() {
			return EnergyReader.SortingType.NAME;
		}

	};
	public boolean sorting_changed = true;

	{
		syncList.addParts(selected, sortingOrder, setting, energyType);
	}

	@Override
	public List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers) {
		handlers.add(EnergyNetworkHandler.INSTANCE);
		return handlers;
	}

	//// ILogicReader \\\\

	@Override
	public int getMaxInfo() {
		return 1;
	}

	@Override
	public AbstractChangeableList<MonitoredEnergyStack> sortMonitoredList(AbstractChangeableList<MonitoredEnergyStack> updateInfo, int channelID) {
		return energy_sorter.sortSaveableList(updateInfo);
	}

	@Override
	public void setMonitoredInfo(AbstractChangeableList<MonitoredEnergyStack> updateInfo, List<NodeConnection> usedChannels, InfoUUID uuid) {
		IInfo info = null;
		switch (setting.getObject()) {
		case STORAGE:
			if (selected.getCoords() != null) {
				for (IMonitoredValue<MonitoredEnergyStack> value : updateInfo.getList()) {
					MonitoredEnergyStack stack = value.getSaveableInfo();
					if (stack.getMonitoredCoords().getCoords().equals(selected.getCoords())) {
						MonitoredEnergyStack convert = stack.copy();
						convert.getEnergyStack().convertEnergyType(energyType.getEnergyType());
						info = convert;
						break;
					}
				}
			}
			break;
		case STORAGES:
			LogicInfoList list = new LogicInfoList(getIdentity(), MonitoredEnergyStack.id, this.getNetworkID());
			list.listSorter = energy_sorter;
			info = list;
			break;
		case TOTAL:
			MonitoredEnergyStack energy = new MonitoredEnergyStack(new StoredEnergyStack(energyType.getEnergyType()), new MonitoredBlockCoords(getCoords(), new ItemStack(PL2Blocks.energy_reader)), new StoredItemStack(new ItemStack(PL2Blocks.energy_reader)));
			for (IMonitoredValue<MonitoredEnergyStack> value : updateInfo.getList()) {
				MonitoredEnergyStack stack = value.getSaveableInfo();
				MonitoredEnergyStack convert = stack.copy();
				convert.getEnergyStack().convertEnergyType(energyType.getEnergyType());
				energy = (MonitoredEnergyStack) energy.joinInfo(convert);
			}
			info = energy;
			break;
		default:
			break;

		}
		ServerInfoHandler.instance().changeInfo(this, uuid, info);

		if (sorting_changed) {
			ServerInfoHandler.instance().markChanged(this, uuid);
			sorting_changed = false;
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
		if (id == sortingOrder.id) {
			sorting_changed = true;
		}
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
		default:
			return null;
		}
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiEnergyReader(player, this);
		default:
			return null;
		}
	}

	@Override
	public ErrorMessage[] getValidMessages() {
		return validStates;
	}

	@Override
	public ILogicListSorter getSorter() {
		return energy_sorter;
	}

}