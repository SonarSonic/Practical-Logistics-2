package sonar.logistics.common.multiparts2;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import sonar.core.network.utils.IByteBufTile;
import sonar.logistics.PL2Multiparts;
import sonar.logistics.api.info.IProvidableInfo;
import sonar.logistics.api.networks.INetworkListHandler;
import sonar.logistics.api.tiles.cable.CableRenderType;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.client.gui.GuiInfoReader;
import sonar.logistics.client.gui.generic.GuiChannelSelection;
import sonar.logistics.common.containers.ContainerChannelSelection;
import sonar.logistics.common.containers.ContainerInfoReader;
import sonar.logistics.common.multiparts2.readers.TileAbstractInfoReader;
import sonar.logistics.connections.handlers.InfoNetworkHandler;
import sonar.logistics.helpers.InfoHelper;

public class TileInfoReader extends TileAbstractInfoReader<IProvidableInfo> {

	@Override
	public List<INetworkListHandler> addValidHandlers(List<INetworkListHandler> handlers) {
		handlers.add(InfoNetworkHandler.INSTANCE);
		return handlers;
	}

	@Override
	public MonitoredList<IProvidableInfo> sortMonitoredList(MonitoredList<IProvidableInfo> updateInfo, int channelID) {
		return updateInfo.setInfo(InfoHelper.sortInfoList(updateInfo));
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
	public CableRenderType getCableRenderSize(EnumFacing dir) {
		return dir == this.getCableFace() ? CableRenderType.HALF // external
				: CableRenderType.NONE; // internal
	}

	@Override
	public PL2Multiparts getMultipart() {
		return PL2Multiparts.INFO_READER;
	}

}
