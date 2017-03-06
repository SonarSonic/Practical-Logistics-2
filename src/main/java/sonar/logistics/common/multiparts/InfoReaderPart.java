package sonar.logistics.common.multiparts;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.helpers.FontHelper;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.LogisticsItems;
import sonar.logistics.api.cabling.ChannelType;
import sonar.logistics.client.gui.GuiChannelSelection;
import sonar.logistics.client.gui.GuiInfoReader;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerInfoReader;
import sonar.logistics.connections.monitoring.InfoMonitorHandler;
import sonar.logistics.connections.monitoring.MonitoredList;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.info.types.LogicInfo;

public class InfoReaderPart extends LogisticsReader<LogicInfo> implements IByteBufTile {

	public InfoReaderPart() {
		super(InfoMonitorHandler.id);
	}

	public InfoReaderPart(EnumFacing face) {
		super(InfoMonitorHandler.id, face);
	}
	
	//// ILogicReader \\\\

	@Override
	public MonitoredList<LogicInfo> sortMonitoredList(MonitoredList<LogicInfo> updateInfo, int channelID) {
		updateInfo.setInfo(InfoHelper.sortInfoList(updateInfo));
		return updateInfo;
	}
	
	//// IChannelledTile \\\\

	@Override
	public ChannelType channelType() {
		return ChannelType.SINGLE;
	}
	
	//// GUI \\\\

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new ContainerInfoReader(player, this);
		case 1:
			return new ContainerChannelSelection(this);
		}
		return null;
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:
			return new GuiInfoReader(player, this);
		case 1:
			return new GuiChannelSelection(player, this, 0);
		}
		return null;
	}

	@Override
	public ItemStack getItemStack() {
		return new ItemStack(LogisticsItems.infoReaderPart);
	}
		
	@Override
	public String getDisplayName() {
		return FontHelper.translate("item.InfoReader.name");
	}

}
