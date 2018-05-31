package sonar.logistics.core.tiles.displays.info.newinfoprofiding.api;

public interface IDataCombinable extends IData {

    /**returns if the data can be combined*/
    boolean canCombine(IDataCombinable data);

    /**returns if the data was changed*/
    boolean doCombine(IDataCombinable data);

}
