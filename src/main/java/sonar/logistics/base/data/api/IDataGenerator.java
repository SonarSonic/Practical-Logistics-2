package sonar.logistics.base.data.api;

import sonar.logistics.base.data.api.methods.IMethod;
import sonar.logistics.base.data.holders.DataHolder;

import java.util.List;

public interface IDataGenerator<D extends IData> {

    Class<D> getDataType();

    IMethod getDataMethod();

    void generateData(D data, List<DataHolder> validHolders);

    boolean isValidHolder(DataHolder holder);

    //FIXME - WRITE TO NBT / READ FROM NBT

}
