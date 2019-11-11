package sonar.logistics.base.data.api.methods.types;

import net.minecraft.entity.Entity;
import sonar.logistics.base.data.api.IEnvironment;

public interface IMethodEntity<O, E extends Entity> {

    O invoke(IEnvironment environment, E tile);

}
