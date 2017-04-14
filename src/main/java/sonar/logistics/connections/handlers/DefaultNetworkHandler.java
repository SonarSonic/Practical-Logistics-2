package sonar.logistics.connections.handlers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.listener.ListenerList;
import sonar.core.listener.PlayerListener;
import sonar.logistics.PL2;
import sonar.logistics.PL2ASMLoader;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.networks.IEntityMonitorHandler;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.networks.INetworkChannels;
import sonar.logistics.api.networks.INetworkHandler;
import sonar.logistics.api.networks.ITileMonitorHandler;
import sonar.logistics.api.tiles.nodes.BlockConnection;
import sonar.logistics.api.tiles.nodes.EntityConnection;
import sonar.logistics.api.tiles.nodes.NodeConnection;
import sonar.logistics.api.tiles.nodes.NodeConnectionType;
import sonar.logistics.api.tiles.readers.ChannelList;
import sonar.logistics.api.tiles.readers.IInfoProvider;
import sonar.logistics.api.tiles.readers.IListReader;
import sonar.logistics.api.tiles.readers.INetworkReader;
import sonar.logistics.api.utils.ChannelType;
import sonar.logistics.api.utils.MonitoredList;
import sonar.logistics.api.viewers.ILogicListenable;
import sonar.logistics.api.viewers.ListenerType;
import sonar.logistics.api.wireless.IDataEmitter;
import sonar.logistics.common.multiparts.wireless.DataEmitterPart;
import sonar.logistics.helpers.InfoHelper;
import sonar.logistics.network.PacketChannels;
import sonar.logistics.network.PacketMonitoredList;

public abstract class DefaultNetworkHandler implements INetworkHandler {
	
	public static INetworkHandler instance(String id) {
		return PL2ASMLoader.networkHandlers.get(id);
	}

	public int hashCode() {
		return Objects.hashCode(id());
	}

}
