package sonar.logistics.base.data.types.general;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import sonar.logistics.base.data.api.IData;
import sonar.logistics.base.data.api.IDataFactory;

public class PrimitiveDataTypes {


    public static class BooleanData extends GeneralData<Boolean> implements IData {

        public BooleanData(boolean data){
            super(data);
        }

        @Override
        public boolean hasUpdated(Boolean newData, Boolean currentData){
            return newData.booleanValue() != currentData.booleanValue();
        }


    }

    public static class BooleanDataFactory implements IDataFactory<BooleanData> {

        @Override
        public BooleanData create() {
            return new BooleanData(false);
        }

        @Override
        public void save(BooleanData data, String key, NBTTagCompound tag) {
            tag.setBoolean(key, data.data);
        }

        @Override
        public void read(BooleanData data, String key, NBTTagCompound tag) {
            data.data = tag.getBoolean(key);
        }

        @Override
        public void saveUpdate(BooleanData data, ByteBuf buf) {
            buf.writeBoolean(data.data);
        }

        @Override
        public void readUpdate(BooleanData data, ByteBuf buf) {
            data.data = buf.readBoolean();
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == Boolean.class;
        }

        @Override
        public void updateData(BooleanData data, Object obj){
            if(obj instanceof Boolean){
                data.setData((Boolean) obj);
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }

    ////

    public static class IntegerData extends GeneralData<Integer> implements IData {

        public IntegerData(int data){
            super(data);
        }

        @Override
        public boolean hasUpdated(Integer newData, Integer currentData){
            return newData.intValue() != currentData.intValue();
        }

    }

    public static class IntegerDataFactory implements IDataFactory<IntegerData> {

        @Override
        public IntegerData create() {
            return new IntegerData(0);
        }

        @Override
        public void save(IntegerData data, String key, NBTTagCompound tag) {
            tag.setInteger(key, data.data);
        }

        @Override
        public void read(IntegerData data, String key, NBTTagCompound tag) {
            data.data = tag.getInteger(key);
        }

        @Override
        public void saveUpdate(IntegerData data, ByteBuf buf) {
            buf.writeInt(data.data);
        }

        @Override
        public void readUpdate(IntegerData data, ByteBuf buf) {
            data.data = buf.readInt();
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == Integer.class || returnType == Short.class || returnType == Byte.class;
        }

        @Override
        public void updateData(IntegerData data, Object obj){
            if(obj instanceof Integer || obj instanceof Short || obj instanceof Byte){
                data.setData(((Number) obj).intValue());
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }

    ////

    public static class LongData extends GeneralData<Long> implements IData {

        public LongData(long data){
            super(data);
        }

        @Override
        public boolean hasUpdated(Long newData, Long currentData){
            return newData.longValue() != currentData.longValue();
        }

    }

    public static class LongDataFactory implements IDataFactory<LongData> {

        @Override
        public LongData create() {
            return new LongData(0);
        }

        @Override
        public void save(LongData data, String key, NBTTagCompound tag) {
            tag.setLong(key, data.data);
        }

        @Override
        public void read(LongData data, String key, NBTTagCompound tag) {
            data.data = tag.getLong(key);
        }

        @Override
        public void saveUpdate(LongData data, ByteBuf buf) {
            buf.writeLong(data.data);
        }

        @Override
        public void readUpdate(LongData data, ByteBuf buf) {
            data.data = buf.readLong();
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == Long.class;
        }

        @Override
        public void updateData(LongData data, Object obj){
            if(obj instanceof Long){
                data.setData((Long) obj);
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }

    ////

    public static class DoubleData extends GeneralData<Double> implements IData {

        public DoubleData(double data){
            super(data);
        }

        @Override
        public boolean hasUpdated(Double newData, Double currentData){
            return newData.doubleValue() != currentData.doubleValue();
        }

    }

    public static class DoubleDataFactory implements IDataFactory<DoubleData> {

        @Override
        public DoubleData create() {
            return new DoubleData(0);
        }

        @Override
        public void save(DoubleData data, String key, NBTTagCompound tag) {
            tag.setDouble(key, data.data);
        }

        @Override
        public void read(DoubleData data, String key, NBTTagCompound tag) {
            data.data = tag.getDouble(key);
        }

        @Override
        public void saveUpdate(DoubleData data, ByteBuf buf) {
            buf.writeDouble(data.data);
        }

        @Override
        public void readUpdate(DoubleData data, ByteBuf buf) {
            data.data = buf.readDouble();
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == Double.class;
        }

        @Override
        public void updateData(DoubleData data, Object obj){
            if(obj instanceof Double){
                data.setData((Double) obj);
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }

    ////

    public static class FloatData extends GeneralData<Float> implements IData {

        public FloatData(float data){
            super(data);
        }

        @Override
        public boolean hasUpdated(Float newData, Float currentData){
            return newData.floatValue() != currentData.floatValue();
        }

    }

    public static class FloatDataFactory implements IDataFactory<FloatData> {

        @Override
        public FloatData create() {
            return new FloatData(0);
        }

        @Override
        public void save(FloatData data, String key, NBTTagCompound tag) {
            tag.setFloat(key, data.data);
        }

        @Override
        public void read(FloatData data, String key, NBTTagCompound tag) {
            data.data = tag.getFloat(key);
        }

        @Override
        public void saveUpdate(FloatData data, ByteBuf buf) {
            buf.writeFloat(data.data);
        }

        @Override
        public void readUpdate(FloatData data, ByteBuf buf) {
            data.data = buf.readFloat();
        }

        @Override
        public boolean canConvert(Class returnType){
            return returnType == Float.class;
        }

        @Override
        public void updateData(FloatData data, Object obj){
            if(obj instanceof Float){
                data.setData((Float) obj);
                return;
            }
            throw new NullPointerException("INVALID CONVERSION: " + this + " given " + obj);
        }
    }

}
