package sonar.logistics.base.data.api;

import sonar.logistics.base.data.sources.IDataSource;

/**used for generating {@IData} from a specific {@IDataSource}
 * also responsible for creating the correct IDataHolder*/
public interface IDataGenerator<S extends IDataSource, D extends IData, H extends IDataHolder<D>> {

    boolean canGenerateForSource(IDataSource source);

    boolean canGenerateForData(IData data);

    D generateData(H holder, D data, S source);

}
