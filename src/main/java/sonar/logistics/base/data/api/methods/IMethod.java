package sonar.logistics.base.data.api.methods;

import net.minecraft.util.ResourceLocation;
import sonar.logistics.base.data.api.IDataFactory;
import sonar.logistics.base.data.api.IEnvironment;

public interface IMethod<O> {

    ResourceLocation getIdentifier();

    IDataFactory getDataFactory();

    Class<O> getReturnType();

    boolean canInvoke(IEnvironment environment);

    O invoke(IEnvironment environment);

}
