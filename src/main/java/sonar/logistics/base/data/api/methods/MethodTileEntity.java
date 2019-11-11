package sonar.logistics.base.data.api.methods;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import sonar.logistics.base.data.api.IEnvironment;
import sonar.logistics.base.data.api.methods.types.IMethodTileEntity;

public class MethodTileEntity<O,T extends TileEntity> extends MethodAbstract<O> {

    private Class<T> tileEntity;
    private IMethodTileEntity<O, T> method;

    public MethodTileEntity(ResourceLocation identifier, Class<O> returnType, Class<T> tileEntity, IMethodTileEntity<O,T> method) {
        super(identifier, returnType);
        this.tileEntity = tileEntity;
        this.method = method;
    }

    @Override
    public boolean canInvoke(IEnvironment environment) {
        return tileEntity.isInstance(environment.tile());
    }

    @Override
    public O invoke(IEnvironment environment) {
        return method.invoke(environment, (T)environment.tile());
    }
}
