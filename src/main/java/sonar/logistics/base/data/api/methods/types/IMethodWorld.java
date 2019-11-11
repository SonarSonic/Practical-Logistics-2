package sonar.logistics.base.data.api.methods.types;

import net.minecraft.world.World;

public interface IMethodWorld<O> {

    O invoke(World world);

}
