package sonar.logistics.api.register;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

/** used to define the type of class the method/return is applicable for this is to speed up identification but you can use NONE for any type of class if you wish */
public enum RegistryType {
	WORLD(World.class, 0), TILE(TileEntity.class, 5), BLOCK(Block.class, 3), ENTITY(Entity.class, 6), ITEM(Item.class, 7), STATE(IBlockState.class, 4), POS(BlockPos.class, 1), FACE(EnumFacing.class, 2), ITEMSTACK(ItemStack.class, 8), CAPABILITY(Capability.class, 9), NONE(null, 9);
	Class classType;
	public int sortOrder;

	RegistryType(Class classType, int sortOrder) {
		this.classType = classType;
		this.sortOrder = sortOrder;
	}

	public boolean isAssignable(Class<?> toCheck) {
		return classType != null && classType.isAssignableFrom(toCheck);
	}

	public static RegistryType getRegistryType(Class<?> toCheck) {
		for (RegistryType type : values()) {
			if (type.isAssignable(toCheck)) {
				return type;
			}
		}
		return NONE;
	}

	public static ArrayList<Class<?>> buildArrayList() {
		ArrayList<Class<?>> classes = new ArrayList();
		for (RegistryType type : values()) {
			if (type.classType != null) {
				classes.add(type.classType);
			}
		}
		return classes;
	}
}
