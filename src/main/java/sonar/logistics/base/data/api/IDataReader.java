package sonar.logistics.base.data.api;

import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.data.newinfo.INewInfo;

/**takes data and turns it into IInfo form for the Server & Client*/
public interface IDataReader extends IDataWatcher {

    InfoUUID getUUID();

    INewInfo update(INewInfo current);

}
