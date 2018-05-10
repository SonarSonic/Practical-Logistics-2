package sonar.logistics.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.PL2;
import sonar.logistics.api.core.tiles.displays.tiles.IDisplay;
import sonar.logistics.api.core.tiles.readers.ClientLocalProvider;
import sonar.logistics.api.core.tiles.wireless.emitters.ClientWirelessEmitter;
import sonar.logistics.core.tiles.displays.info.types.general.InfoChangeableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInfoHandler extends CommonInfoHandler {

	public Map<Integer, IDisplay> displays_tile = new HashMap<>();
	public Map<Integer, NBTTagCompound> invalid_gsi = new HashMap<>();

	public Map<Integer, List<Object>> sortedLogicMonitors = new ConcurrentHashMap<>();
	public Map<Integer, List<ClientLocalProvider>> clientLogicMonitors = new ConcurrentHashMap<>();

	public Map<Integer, InfoChangeableList> channelMap = new ConcurrentHashMap<>();
	public List<ClientWirelessEmitter> clientDataEmitters = new ArrayList<>();
	public List<ClientWirelessEmitter> clientRedstoneEmitters = new ArrayList<>();

	public static ClientInfoHandler instance() {
		return PL2.proxy.getClientManager();
	}

	public ClientInfoHandler() {
		super(Side.CLIENT);
	}

	@Override
	public void removeAll() {
		super.removeAll();
		displays_tile.clear();
		invalid_gsi.clear();

		sortedLogicMonitors.clear();
		clientLogicMonitors.clear();

		channelMap.clear();
		clientDataEmitters.clear();
		clientRedstoneEmitters.clear();
	}

}
