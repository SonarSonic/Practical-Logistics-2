package sonar.logistics.base.data.api.methods;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import sonar.logistics.base.data.api.IEnvironment;
import sonar.logistics.base.data.api.methods.types.IMethodEntity;

public class MethodEntity<O, E extends Entity> extends MethodAbstract<O> {

    private Class<E> entity;
    private IMethodEntity<O, E> method;

    public MethodEntity(ResourceLocation identifier, Class<O> returnType, Class<E> entity, IMethodEntity<O,E> method) {
        super(identifier, returnType);
        this.entity = entity;
        this.method = method;
    }

    @Override
    public boolean canInvoke(IEnvironment environment) {
        return entity.isInstance(environment.tile());
    }

    @Override
    public O invoke(IEnvironment environment) {
        return method.invoke(environment, (E)environment.entity());
    }
}
