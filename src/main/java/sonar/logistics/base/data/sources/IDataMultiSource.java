package sonar.logistics.base.data.sources;

import java.util.List;

/**should only be used with combinable info, otherwise the first source will be the only one which is used*/
public interface IDataMultiSource extends IDataSource {

    List<IDataSource> getDataSources();

    int getIdentity();

}
