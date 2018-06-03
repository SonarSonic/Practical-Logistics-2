package sonar.logistics;

import sonar.logistics.api.core.tiles.connections.data.network.ILogisticsNetwork;
import sonar.logistics.api.core.tiles.connections.redstone.IRedstoneConnectable;
import sonar.logistics.api.core.tiles.connections.redstone.network.IRedstoneNetwork;
import sonar.logistics.base.events.types.NetworkEvent;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;

public class PL2Logging {

    public static void onGSIValidated(DisplayGSI gsi){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Validated GSI: " + gsi.getDisplayGSIIdentity() + " Client: " + gsi.getWorld().isRemote);
        }
    }

    public static void onGSIInvalidated(DisplayGSI gsi){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Invalidated GSI: " + gsi.getDisplayGSIIdentity() + " Client: " + gsi.getWorld().isRemote);
        }
    }

    public static void onGSIElementValidated(DisplayGSI gsi, IDisplayElement element){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Validated Element: " + element.getElementIdentity() + " Client: " + gsi.getWorld().isRemote);
        }
    }

    public static void onGSIElementInvalidated(DisplayGSI gsi, IDisplayElement element){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Invalidated Element: " + element.getElementIdentity() + " Client: " + gsi.getWorld().isRemote);
        }
    }

    public static void disconnectFromWrongDataNetwork(INetworkTile tile, ILogisticsNetwork current, ILogisticsNetwork attempted){
        if(PL2Config.log_connection_events) {
            PL2.logger.info(tile.getIdentity() + " : attempted to disconnect from the wrong data network with ID: " + attempted.getNetworkID() + " expected " + current.getNetworkID());
        }
    }

    public static void disconnectFromWrongRedstoneNetwork(IRedstoneConnectable tile, IRedstoneNetwork current, IRedstoneNetwork attempted){
        if(PL2Config.log_connection_events) {
            PL2.logger.info(tile.getIdentity() + " : attempted to disconnect from the wrong redstone network with ID: " + attempted.getNetworkID() + " expected " + current.getNetworkID());
        }
    }

    public static void logConnectedNetworkEvent(NetworkEvent.ConnectedNetwork event){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Networks Connected: " + event.network.getNetworkID() + " " + event.connected_network.getNetworkID());
        }
    }

    public static void logDisconnectedNetworkEvent(NetworkEvent.DisconnectedNetwork event){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Networks Disconnected: " + event.network.getNetworkID() + " " + event.disconnected_network.getNetworkID());
        }
    }

    public static void logConnectedTileEvent(NetworkEvent.ConnectedTile event){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Tile Connected: " + event.tile.getIdentity() + " " + event.tile);
        }
    }

    public static void logDisconnectedTileEvent(NetworkEvent.DisconnectedTile event){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Tile Disconnected: " + event.tile.getIdentity() + " " + event.tile);
        }
    }

    public static void logConnectedLocalProviderEvent(NetworkEvent.ConnectedLocalProvider event){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Local Provider Connected: " + event.tile.getIdentity() + " " + event.tile);
        }
    }

    public static void logDisconnectedLocalProviderEvent(NetworkEvent.DisconnectedLocalProvider event){
        if(PL2Config.log_connection_events) {
            PL2.logger.info("Local Provider Disconnected: " + event.tile.getIdentity() + " " + event.tile);
        }
    }
}
