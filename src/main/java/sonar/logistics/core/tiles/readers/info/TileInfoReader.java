package sonar.logistics.core.tiles.readers.info;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.core.tiles.displays.info.IProvidableInfo;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.api.core.tiles.readers.ILogicListSorter;
import sonar.logistics.api.core.tiles.readers.channels.INetworkHandler;
import sonar.logistics.base.channels.ChannelType;
import sonar.logistics.base.channels.ContainerChannelSelection;
import sonar.logistics.base.channels.GuiChannelSelection;
import sonar.logistics.core.tiles.readers.base.TileAbstractLogicReader;
import sonar.logistics.core.tiles.readers.info.handling.InfoNetworkHandler;

import java.util.List;

public class TileInfoReader extends TileAbstractLogicReader<IProvidableInfo> {

	public InfoSorter info_sorter = new InfoSorter();
	
	@Override
	public int getMaxInfo() {
		return 4;
	}

	@Override
	public List<INetworkHandler> addValidHandlers(List<INetworkHandler> handlers) {
		handlers.add(InfoNetworkHandler.INSTANCE);
		return handlers;
	}

	@Override
	public AbstractChangeableList<IProvidableInfo> sortMonitoredList(AbstractChangeableList<IProvidableInfo> updateInfo, int channelID) {
		return info_sorter.sortSaveableList(updateInfo);
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

	@Override
	public ILogicListSorter getSorter() {
		return info_sorter;
	}

}
