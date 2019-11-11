package sonar.logistics.base.data.api.methods;

import net.minecraft.util.ResourceLocation;
import sonar.logistics.base.data.DataManager;
import sonar.logistics.base.data.api.IDataFactory;

public abstract class MethodAbstract<O> implements IMethod<O> {

    private Class<O> returnType;
    private IDataFactory factory;
    private ResourceLocation identifier;

    public MethodAbstract(ResourceLocation identifier, Class<O> returnType){
        this.identifier = identifier;
        this.returnType = returnType;
        this.factory = DataManager.getFactoryForPrimitive(returnType);
    }

    @Override
    public Class<O> getReturnType() {
        return returnType;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return identifier;
    }

    public IDataFactory getDataFactory(){
        return factory;
    }
}
