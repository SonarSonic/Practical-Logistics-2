package sonar.logistics.common.multiparts;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.api.energy.EnergyType;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.network.sync.SyncEnum;
import sonar.core.network.sync.SyncGeneric;
import sonar.core.network.sync.SyncTagType;
import sonar.core.network.sync.SyncTagType.INT;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.utils.SortingDirection;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.api.nodes.NodeConnection;
import sonar.logistics.api.readers.EnergyReader;
import sonar.logistics.api.readers.FluidReader;
import sonar.logistics.client.gui.GuiEnergyReader;
import sonar.logistics.common.containers.ContainerEnergyReader;
import sonar.logistics.connections.monitoring.EnergyMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredEnergyStack;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.EnergyHelper;
import sonar.logistics.network.SyncMonitoredType;

public class EnergyReaderPart extends ReaderMultipart<MonitoredEnergyStack> implements IByteBufTile {

	public SyncMonitoredType<MonitoredEnergyStack> selected = new SyncMonitoredType<MonitoredEnergyStack>(1);
	public SyncEnum<SortingDirection> sortingOrder = (SyncEnum) new SyncEnum(SortingDirection.values(), 2).addSyncType(SyncType.SPECIAL);
	public SyncEnum<EnergyReader.SortingType> sortingType = (SyncEnum) new SyncEnum(EnergyReader.SortingType.values(), 3).addSyncType(SyncType.SPECIAL);
	{
		syncList.addParts(selected, sortingOrder, sortingType);
	}
	public EnergyReaderPart() {
		super(EnergyMonitorHandler.id);
	}

	public EnergyReaderPart(EnumFacing face) {
		super(EnergyMonitorHandler.id, face);
	}

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.energyReaderPart);
	}
	
	@Override
	public MonitoredList<MonitoredEnergyStack> sortMonitoredList(MonitoredList<MonitoredEnergyStack> updateInfo, int channelID) {
		EnergyHelper.sortEnergyList(updateInfo, sortingOrder.getObject(), sortingType.getObject());
		return updateInfo;
	}

	@Override
	public ChannelType channelType() {
		return ChannelType.UNLIMITED;
	}

	@Override
	public void setMonitoredInfo(MonitoredList<MonitoredEnergyStack> updateInfo, ArrayList<NodeConnection> connections, ArrayList<Entity> entities, int channelID) {
		/*
		IMonitorInfo info = null;
		switch (setting.getObject()) {
		case FLUID:
			MonitoredFluidStack stack = selected.getMonitoredInfo();
			if (stack != null) {
				MonitoredFluidStack dummyInfo = stack.copy();
				Pair<Boolean, IMonitorInfo> latestInfo = updateInfo.getLatestInfo(dummyInfo);
				info = latestInfo.a ? latestInfo.b : dummyInfo;
			}
			break;
		case POS:
			break;
		case STORAGE:
			break;
		case TANKS:
			break;
		default:
			break;
		}
		if (info != null) {
			InfoUUID id = new InfoUUID(getMonitorUUID().hashCode(), 0);
			IMonitorInfo oldInfo = LogicMonitorCache.info.get(id);
			if (oldInfo == null || !oldInfo.isIdenticalInfo(info)) {
				LogicMonitorCache.changeInfo(id, info);
			}
		}
		*/
	}
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
	public String getDisplayName() {
		return FontHelper.translate("item.EnergyReader.name");
	}
}
