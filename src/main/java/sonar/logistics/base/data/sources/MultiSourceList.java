package sonar.logistics.base.data.sources;

import java.util.ArrayList;
import java.util.List;

public class MultiSourceList {

    private List<IDataSource> dataSources = new ArrayList<>();

    public void addDataSource(IDataSource dataSource){
        dataSources.add(dataSource);
    }

    public void removeDataSource(IDataSource dataSource){
        dataSources.remove(dataSource);
    }

    public List<IDataSource> getSources(){
        return dataSources;
    }

}
