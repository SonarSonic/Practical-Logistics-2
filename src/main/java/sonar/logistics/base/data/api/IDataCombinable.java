package sonar.logistics.base.data.api;

public interface IDataCombinable<D extends IDataCombinable> extends IData {

    /**returns if the data can be combined*/
    boolean canCombine(IDataCombinable data);

    /**returns if the data was changed*/
    boolean doCombine(D data);

}
