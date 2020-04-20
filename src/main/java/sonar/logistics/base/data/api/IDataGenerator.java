package sonar.logistics.base.data.api;

import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.sources.IDataSource;

/**used for generating {@IData} from a specific {@IDataSource}
 * also responsible for creating the correct IDataHolder*/
public interface IDataGenerator<S extends IDataSource, D extends IData> {

    boolean canGenerateForSource(IDataSource source);

    boolean updateData(DataHolder holder, D data, S source);

}
