package sonar.logistics.base.data.sources;

import sonar.logistics.base.channels.ChannelList;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.CacheType;

import java.util.ArrayList;
import java.util.List;

public class MultiDataSource implements IDataMultiSource {

    public final ChannelList list;
    public final INetworkTile tile;

    public MultiDataSource(ChannelList list, INetworkTile tile){
        this.list = list;
        this.tile = tile;
    }

    @Override
    public List<IDataSource> getDataSources() {
        if(tile.getNetwork().isValid()){
            List<IDataSource> sources = new ArrayList<>();
            sources.addAll(tile.getNetwork().getConnections(CacheType.GLOBAL));
            return sources;
        }
        return new ArrayList<>();
    }

}
