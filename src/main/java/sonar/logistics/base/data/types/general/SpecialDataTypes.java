package sonar.logistics.base.data.types.general;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import sonar.logistics.base.data.api.IData;
import sonar.logistics.base.data.api.IDataFactory;

public class SpecialDataTypes {

    ////

    public static class StringData extends GeneralData<String> implements IData {

        public StringData(String data){
            super(data);
        }

        @Override
        public boolean hasUpdated(String newData, String currentData){
            return !currentData.equals(newData);
        }

    }

    public static class StringDataFactory implements IDataFactory<StringData> {

        @Override
        public StringData create() {
            return new StringData("");
        }

        @Override
        public void save(StringData data, String key, NBTTagCompound tag) {
            tag.setString(key, data.data);
        }

        @Override
        public void read(StringData data, String key, NBTTagCompound tag) {
            data.data = tag.getString(key);
        }

        @Override
        public void saveUpdate(StringData data, ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, data.data);
        }

        @Override
        public void readUpdate(StringData data, ByteBuf buf) {
            data.data = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == String.class;
        }

        @Override
        public void updateData(StringData data, Object obj){
            if(obj instanceof String){
                data.setData((String) obj);
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }

    ////


    public static class ItemStackData extends GeneralData<ItemStack> implements IData {

        public ItemStackData(ItemStack data){
            super(data);
        }

        @Override
        public boolean hasUpdated(ItemStack newData, ItemStack currentData){
            return !ItemStack.areItemStacksEqual(newData, currentData) || !ItemStack.areItemStackTagsEqual(newData, currentData);
        }

    }

    public static class ItemStackDataFactory implements IDataFactory<ItemStackData> {

        @Override
        public ItemStackData create() {
            return new ItemStackData(ItemStack.EMPTY);
        }

        @Override
        public void save(ItemStackData data, String key, NBTTagCompound tag) {
            tag.setTag(key, data.data.writeToNBT(new NBTTagCompound()));
        }

        @Override
        public void read(ItemStackData data, String key, NBTTagCompound tag) {
            data.data = new ItemStack(tag.getCompoundTag(key));
        }

        @Override
        public void saveUpdate(ItemStackData data, ByteBuf buf) {
            ByteBufUtils.writeItemStack(buf, data.data);
        }

        @Override
        public void readUpdate(ItemStackData data, ByteBuf buf) {
            data.data = ByteBufUtils.readItemStack(buf);
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == ItemStack.class;
        }

        @Override
        public void updateData(ItemStackData data, Object obj){
            if(obj instanceof ItemStack){
                data.setData((ItemStack) obj);
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }

    ////

    public static class NBTData extends GeneralData<NBTTagCompound> implements IData {

        public NBTData(NBTTagCompound data){
            super(data);
        }

        @Override
        public boolean hasUpdated(NBTTagCompound newData, NBTTagCompound currentData){
            return !newData.equals(currentData);
        }

    }

    public static class NBTDataFactory implements IDataFactory<NBTData> {

        @Override
        public NBTData create() {
            return new NBTData(new NBTTagCompound());
        }

        @Override
        public void save(NBTData data, String key, NBTTagCompound tag) {
            tag.setTag(key, data.data);
        }

        @Override
        public void read(NBTData data, String key, NBTTagCompound tag) {
            data.data = tag.getCompoundTag(key);
        }

        @Override
        public void saveUpdate(NBTData data, ByteBuf buf) {
            ByteBufUtils.writeTag(buf, data.data);
        }

        @Override
        public void readUpdate(NBTData data, ByteBuf buf) {
            data.data = ByteBufUtils.readTag(buf);
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == NBTTagCompound.class;
        }

        @Override
        public void updateData(NBTData data, Object obj){
            if(obj instanceof NBTTagCompound){
                data.setData((NBTTagCompound) obj);
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }
}
