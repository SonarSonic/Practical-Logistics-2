package sonar.logistics.base.data.api.methods.types;

import net.minecraft.block.Block;
import sonar.logistics.base.data.api.IEnvironment;

public interface IMethodBlock<O, B extends Block> {

    O invoke(IEnvironment environment, B block);

}
