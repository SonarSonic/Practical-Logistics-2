package sonar.logistics.info.registries;

import java.util.HashMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import sonar.logistics.PL2;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.IInfoRegistry;
import sonar.logistics.api.info.ILogicInfoRegistry;
import sonar.logistics.api.register.RegistryType;

@InfoRegistry(modid = PL2.MODID)
public class VanillaInfoRegistry extends IInfoRegistry {

	@Override
	public void registerBaseReturns(ILogicInfoRegistry registry) {
		registry.registerReturn(WorldInfo.class);
		registry.registerReturn(WorldProvider.class);
		registry.registerReturn(DimensionType.class);
		registry.registerReturn(Fluid.class);
		registry.registerReturn(FoodStats.class);
		registry.registerReturn(Team.class);
		registry.registerReturn(NBTTagCompound.class);
		registry.registerReturn(BlockPos.class);
	}

	@Override
	public void registerBaseMethods(ILogicInfoRegistry registry) {
		registry.registerMethods(Block.class, RegistryType.BLOCK, Lists.newArrayList("getUnlocalizedName", "getMetaFromState", "getHarvestLevel", "isFoliage", "isWood", "canSustainLeaves"));
		registry.registerMethods(Block.class, RegistryType.BLOCK, Lists.newArrayList("getWeakPower", "getStrongPower", "isSideSolid", "getBlockHardness"));
		registry.registerMethods(BlockFluidBase.class, RegistryType.BLOCK, Lists.newArrayList("getFluid"));
		registry.registerMethods(BlockCrops.class, RegistryType.BLOCK, Lists.newArrayList("isMaxAge"));
		registry.registerMethods(Fluid.class, RegistryType.BLOCK, Lists.newArrayList("getLuminosity", "getDensity", "getTemperature", "getViscosity"));
		registry.registerMethods(BlockPos.class, RegistryType.POS, Lists.newArrayList("getX", "getY", "getZ"));
		registry.registerMethods(EnumFacing.class, RegistryType.FACE, Lists.newArrayList("toString"));
		registry.registerMethods(World.class, RegistryType.WORLD, Lists.newArrayList("isBlockIndirectlyGettingPowered", "getWorldInfo"));
		registry.registerMethods(WorldInfo.class, RegistryType.WORLD, Lists.newArrayList("isRaining", "isThundering", "getWorldName"));
		registry.registerMethods(WorldProvider.class, RegistryType.WORLD, Lists.newArrayList("getDimension", "getDimensionType"));
		registry.registerMethods(DimensionType.class, RegistryType.WORLD, Lists.newArrayList("getName"));
		registry.registerMethods(Entity.class, RegistryType.ENTITY, Lists.newArrayList("getPosition", "getName"));
		registry.registerMethods(EntityLivingBase.class, RegistryType.ENTITY, Lists.newArrayList("getHealth", "getMaxHealth", "getAge", "getTotalArmorValue"));
		registry.registerMethods(EntityAgeable.class, RegistryType.ENTITY, Lists.newArrayList("getGrowingAge"));
		registry.registerMethods(EntityPlayer.class, RegistryType.ENTITY, Lists.newArrayList("isCreative", "isSpectator", "getFoodStats", "getAbsorptionAmount", "getTeam", "getExperiencePoints"));
		registry.registerMethods(FoodStats.class, RegistryType.ENTITY, Lists.newArrayList("getFoodLevel", "needFood", "getSaturationLevel"));
		registry.registerMethods(Team.class, RegistryType.ENTITY, Lists.newArrayList("getRegisteredName"));
		registry.registerMethods(ItemStack.class, RegistryType.ITEMSTACK, Lists.newArrayList("getItem", "getMaxStackSize", "getTagCompound"));
	}

	@Override
	public void registerAllFields(ILogicInfoRegistry registry) {
		HashMap<String, Integer> furnaceFields = Maps.<String, Integer>newHashMap();
		furnaceFields.put("furnaceBurnTime", 0);
		furnaceFields.put("currentItemBurnTime", 1);
		furnaceFields.put("cookTime", 2);
		furnaceFields.put("totalCookTime", 3);
		registry.registerInvFields(TileEntityFurnace.class, furnaceFields);
		registry.registerFields(TileEntityNote.class, RegistryType.TILE, Lists.newArrayList("note"));
		registry.registerFields(ItemStack.class, RegistryType.ITEMSTACK, Lists.newArrayList("stackSize"));
	}

	@Override
	public void registerAdjustments(ILogicInfoRegistry registry) {
		registry.registerInfoAdjustments(Lists.newArrayList("EntityLivingBase.getHealth", "EntityLivingBase.getMaxHealth"), "", "HP");
		registry.registerInfoAdjustments(Lists.newArrayList("TileEntityFurnace.furnaceBurnTime", "TileEntityFurnace.currentItemBurnTime", "TileEntityFurnace.cookTime", "TileEntityFurnace.totalCookTime"), "", "ticks");
		// LogicRegistry.registerInfoAdjustments(Lists.newArrayList("Block.getUnlocalizedName"), "", ".name");
	}

}
