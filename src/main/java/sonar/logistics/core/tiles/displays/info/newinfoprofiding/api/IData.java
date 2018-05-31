package sonar.logistics.core.tiles.displays.info.newinfoprofiding.api;

import sonar.logistics.core.tiles.displays.info.newinfoprofiding.DataFactory;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.holders.DataHolder;
import sonar.logistics.core.tiles.displays.info.newinfoprofiding.sources.IDataSource;

import javax.annotation.Nullable;
import java.util.Optional;

/**represents any type of data obtained via a {@link IDataGenerator}*/
public interface IData {

    @Nullable
    default <T extends IData> IDataHolder<T> createHolder(IDataSource source, T freshData, int tickRate){
        Optional<IDataGenerator> generator = DataFactory.instance().getValidGenerator(source, freshData);
        return generator.isPresent() ? new DataHolder(generator.get(), source, freshData, tickRate) : null;
    }

}
