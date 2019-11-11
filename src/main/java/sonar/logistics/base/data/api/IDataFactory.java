package sonar.logistics.base.data.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public interface IDataFactory<D extends IData>{

        D create();

        void save(D data, String key, NBTTagCompound tag);

        void read(D data, String key, NBTTagCompound tag);

        void saveUpdate(D data, ByteBuf buf);

        void readUpdate(D data, ByteBuf buf);

        default boolean canConvert(Class returnType){
                return false;
        }

        default void updateData(D data, Object obj){}
}