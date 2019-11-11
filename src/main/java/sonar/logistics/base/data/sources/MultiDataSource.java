package sonar.logistics.base.data.sources;

import sonar.logistics.base.ServerInfoHandler;
import sonar.logistics.base.channels.BlockConnection;
import sonar.logistics.base.channels.NodeConnection;

import java.util.ArrayList;
import java.util.List;

public class MultiDataSource implements IDataMultiSource {

    public int identity;
    private List<IDataSource> dataSources;

    public MultiDataSource(List<IDataSource> dataSources){
        this.dataSources = dataSources;
        this.identity = ServerInfoHandler.instance().getNextIdentity();
    }

    @Override
    public List<IDataSource> getDataSources() {
        return dataSources;
    }

    @Override
    public int getIdentity() {
        return identity;
    }

    public void addDataSource(IDataSource dataSource){
        dataSources.add(dataSource);
    }

    public void removeDataSource(IDataSource dataSource){
        dataSources.remove(dataSource);
    }

    @Override
    public int hashCode(){
        return getIdentity();
    }

    @Override
    public boolean equals(Object object){
        return object instanceof MultiDataSource && ((MultiDataSource) object).identity == identity;
    }

    //TODO DELETE ME - MAKE PROPER SOURCE SYSTEM
    public static List<SourceCoord4D> getDirtyConversion(List<NodeConnection> nodes){
        List<SourceCoord4D> coord4DS = new ArrayList<>();
        nodes.forEach(node -> {
            if(node instanceof BlockConnection) {
                BlockConnection connection = (BlockConnection) node;
                coord4DS.add(new SourceCoord4D(connection.coords.getX(),connection.coords.getY(),connection.coords.getZ(),connection.coords.getDimension(), connection.face));
            }
        });
        return coord4DS;
    }

}
