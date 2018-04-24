package sonar.logistics.api.info.handlers;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import sonar.logistics.api.info.IProvidableInfo;

public interface IEntityInfoProvider {

	/** @param world the world
	 * @param entity the entity
	 * @return if this handler can provide info on the given entity */
    boolean canProvide(World world, Entity entity);

	/** allows you to add all types of info for a given Entity for use in the Info Reader
	 * @param infoList the current info list
	 * @param world the world
	 * @param entity the entity */
    void provide(List<IProvidableInfo> infoList, World world, Entity entity);
}
