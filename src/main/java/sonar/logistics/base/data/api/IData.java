package sonar.logistics.base.data.api;

import sonar.logistics.base.data.DataFactory;
import sonar.logistics.base.data.holders.DataHolder;
import sonar.logistics.base.data.sources.IDataSource;

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
