package sonar.logistics.base.data.api;

public interface IDataFactory<D extends IData>{

        D create();

}