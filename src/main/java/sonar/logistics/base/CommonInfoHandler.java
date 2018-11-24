package sonar.logistics.base;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.api.base.IInfoManager;
import sonar.logistics.api.core.tiles.displays.info.IInfo;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.api.core.tiles.displays.info.lists.AbstractChangeableList;
import sonar.logistics.base.events.types.InfoEvent;
import sonar.logistics.base.listeners.ILogicListenable;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;

import java.util.HashMap;
import java.util.Map;

public abstract class CommonInfoHandler implements IInfoManager {

    public final Map<InfoUUID, IInfo> infoMap = new HashMap<>();
    public final Map<Integer, DisplayGSI> gsiMap = new HashMap<>();
    public final Map<Integer, ILogicListenable> identityTiles = new HashMap<>();
    public final Map<Integer, ConnectedDisplay> connectedDisplays = new HashMap<>();
    public final Map<InfoUUID, AbstractChangeableList> changeableLists = new HashMap<>();
    public final Side side;

    public CommonInfoHandler(Side side){
        this.side = side;
    }

    public boolean isRemote(){
        return side.isClient();
    }

    @Override
    public void removeAll(){
        infoMap.clear();
        gsiMap.clear();
        identityTiles.clear();
        connectedDisplays.clear();
        changeableLists.clear();;
    }

    @Override
    public void setInfo(InfoUUID uuid, IInfo newInfo) {
        infoMap.put(uuid, newInfo);
        MinecraftForge.EVENT_BUS.post(new InfoEvent.InfoChanged(newInfo, uuid, isRemote()));
    }

    @Override
    public void addIdentityTile(ILogicListenable infoProvider, PL2AdditionType type) {
        identityTiles.put(infoProvider.getIdentity(), infoProvider);
    }

    @Override
    public void removeIdentityTile(ILogicListenable monitor, PL2RemovalType type) {
        ILogicListenable loaded = identityTiles.get(monitor.getIdentity());
        if(monitor == loaded){ // due to queue sometimes different instances are unloaded and loaded in the same tick.
            identityTiles.remove(monitor.getIdentity());
        }
    }

    @Override
    public final Map<InfoUUID, IInfo> getInfoMap() {
        return infoMap;
    }

    @Override
    public final Map<Integer, DisplayGSI> getGSIMap() {
        return gsiMap;
    }

    @Override
    public final Map<Integer, ILogicListenable> getNetworkTileMap() {
        return identityTiles;
    }

    @Override
    public final Map<Integer, ConnectedDisplay> getConnectedDisplays() {
        return connectedDisplays;
    }

    @Override
    public final Map<InfoUUID, AbstractChangeableList> getChangeableListMap() {
        return changeableLists;
    }
}
