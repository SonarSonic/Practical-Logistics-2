package sonar.logistics.base.data.api;

/**represents any type of data obtained via a {@link IDataGenerator}*/
public interface IData {

    default void preUpdate(){}

    default void postUpdate(){}

    /**called after the packet is sent*/
    default void onUpdated(){}

    default boolean hasUpdated(){
        return true;
    }



}
