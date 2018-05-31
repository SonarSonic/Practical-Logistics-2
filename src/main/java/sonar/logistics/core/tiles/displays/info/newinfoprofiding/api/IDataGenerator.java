package sonar.logistics.core.tiles.displays.info.newinfoprofiding.api;

import sonar.logistics.core.tiles.displays.info.newinfoprofiding.sources.IDataSource;

/**used for generating {@IData} from a specific {@IDataSource}
 * also responsible for creating the correct IDataHolder*/
public interface IDataGenerator<S extends IDataSource, D extends IData, H extends IDataHolder<D>> {

    boolean canGenerateForSource(IDataSource source);

    boolean canGenerateForData(IData data);

    D generateData(H holder, D data, S source);

}
