package sonar.logistics.info.registries;

import com.google.common.collect.Lists;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.register.IInfoRegistry;
import sonar.logistics.api.info.register.IMasterInfoRegistry;
import sonar.logistics.api.info.register.RegistryType;
import sonar.logistics.common.hammer.TileEntityHammer;

@InfoRegistry(modid = PL2Constants.MODID)
public class LogisticsInfoRegistry implements IInfoRegistry {

	///NETWORK READER	
	public static final String PL_TICK_TIME = "ILogisticsNetwork.pl2TickTime";
	public static final String PL_TICK_PERCENT = "ILogisticsNetwork.pl2TickPercentage";
	public static final String PL_TICK_FURNACE = "ILogisticsNetwork.pl2TickFurnace";
	public static final String PL_NETWORK_TICK_TIME = "ILogisticsNetwork.getNetworkTickTime";
	public static final String PL_NETWORK_TICK_PERCENT = "ILogisticsNetwork.percent";
	public static final String PL_NETWORK_TICK_FURNACE = "ILogisticsNetwork.furnace";
	public static final String PL_NETWORK_ID = "ILogisticsNetwork.getNetworkID";
	public static final String PL_CONNECTED_NETWORKS = "ILogisticsNetwork.connectedNetworks";
	public static final String PL_WATCHING_NETWORKS = "ILogisticsNetwork.watchingNetworks";
	public static final String PL_TOTAL_CONNECTIONS = "ILogisticsNetwork.totalConnections";
	public static final String PL_LOCAL_CONNECTIONS = "ILogisticsNetwork.localConnections";
	public static final String PL_GLOBAL_CONNECTIONS = "ILogisticsNetwork.globalConnections";
	
	public static final String PL_CACHE_CABLES = "ILogisticsNetwork.CACHE.IDataCable";
	public static final String PL_CACHE_TILES = "ILogisticsNetwork.CACHE.INetworkListener";
	public static final String PL_CACHE_NODES = "ILogisticsNetwork.CACHE.INode";
	public static final String PL_CACHE_ENTITY_NODES = "ILogisticsNetwork.CACHE.IEntityNode";
	public static final String PL_CACHE_READERS = "ILogisticsNetwork.CACHE.IListReader";
	public static final String PL_CACHE_EMITTERS = "ILogisticsNetwork.CACHE.IDataEmitter";
	public static final String PL_CACHE_RECEIVERS = "ILogisticsNetwork.CACHE.IDataReceiver";
	public static final String PL_CACHE_TRANSFER_NODES = "ILogisticsNetwork.CACHE.ITransferNode";

	// public static final String PL_WATCHING_NETWORKS = "ILogisticsNetwork.watchingNetworks";

	@Override
	public void registerBaseMethods(IMasterInfoRegistry registry) {
		registry.registerMethods(TileEntityHammer.class, RegistryType.TILE, Lists.newArrayList("getSpeed", "getProgress", "getCoolDown", "getCoolDownSpeed"));
	}

	@Override
	public void registerAdjustments(IMasterInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("TileEntityHammer.getSpeed", "TileEntityHammer.getProgress", "TileEntityHammer.getCoolDown", "TileEntityHammer.getCoolDownSpeed"), "", "ticks");
		registry.registerInfoAdjustments("item.storage", "", "items");
		registry.registerInfoAdjustments("fluid.storage", "", "mb");
		
		//NETWORK READER
		registry.registerInfoAdjustments(Lists.newArrayList(PL_TICK_TIME, PL_NETWORK_TICK_TIME), "", "ms");
		registry.registerInfoAdjustments(Lists.newArrayList(PL_TICK_PERCENT, PL_NETWORK_TICK_PERCENT), "", "%");
		registry.registerInfoAdjustments(Lists.newArrayList(PL_TICK_FURNACE, PL_NETWORK_TICK_FURNACE), "", "furnaces");
	}	
}
