package sonar.logistics.base.data.api.methods;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import sonar.logistics.base.data.api.IEnvironment;
import sonar.logistics.base.data.api.methods.types.IMethodBlock;

public class MethodBlock<O,B extends Block> extends MethodAbstract<O> {

    private Class<B> block;
    private IMethodBlock<O, B> method;

    public MethodBlock(ResourceLocation identifier, Class<O> returnType, Class<B> block, IMethodBlock<O,B> method) {
        super(identifier, returnType);
        this.block = block;
        this.method = method;
    }

    @Override
    public boolean canInvoke(IEnvironment environment) {
        return environment.state() != null && block.isInstance(environment.state().getBlock());
    }

    @Override
    public O invoke(IEnvironment environment) {
        return method.invoke(environment, (B)environment.state().getBlock());
    }
}
