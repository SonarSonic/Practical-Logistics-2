package sonar.logistics.common.multiparts.readers;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.client.gui.GuiInfoReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerInfoReader;
import sonar.logistics.networking.info.InfoHelper;
import sonar.logistics.networking.info.InfoNetworkHandler;

public class TileInfoReader extends TileAbstractLogicReader<IProvidableInfo> {

	@Override
	public List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers) {
		handlers.add(InfoNetworkHandler.INSTANCE);
		return handlers;
	}

	@Override
	public AbstractChangeableList<IProvidableInfo> sortMonitoredList(AbstractChangeableList<IProvidableInfo> updateInfo, int channelID) {
		return InfoHelper.sortInfoList(updateInfo);
	}

	@Override
	public ChannelType channelType() {
		return ChannelType.SINGLE;
	}

	@Override
	public Object getServerElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:	return new ContainerInfoReader(player, this);
		case 1:	return new ContainerChannelSelection(this);
		default: return null;}
	}

	@Override
	public Object getClientElement(Object obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		switch (id) {
		case 0:	return new GuiInfoReader(player, this);
		case 1:	return new GuiChannelSelection(player, this, 0);
		default: return null;}
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.INFO_READER;
	}

}
