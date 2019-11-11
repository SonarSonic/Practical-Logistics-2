package sonar.logistics.base.data.api.methods.types;

import net.minecraft.tileentity.TileEntity;
import sonar.logistics.base.data.api.IEnvironment;

public interface IMethodTileEntity<O, T extends TileEntity> {

    O invoke(IEnvironment environment, T tile);

}
