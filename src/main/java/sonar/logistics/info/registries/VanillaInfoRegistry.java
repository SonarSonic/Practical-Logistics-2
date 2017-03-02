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
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.InfoRegistry;
import sonar.logistics.api.info.IInfoRegistry;
import static sonar.logistics.info.LogicInfoRegistry.*;
import static sonar.logistics.info.LogicInfoRegistry.RegistryType.*;

@InfoRegistry(modid = Logistics.MODID)
public class VanillaInfoRegistry extends IInfoRegistry {

	@Override
	public void registerBaseReturns() {
		registerReturn(WorldInfo.class);
		registerReturn(WorldProvider.class);
		registerReturn(DimensionType.class);
		registerReturn(Fluid.class);
		registerReturn(FoodStats.class);
		registerReturn(Team.class);
		registerReturn(NBTTagCompound.class);
		registerReturn(BlockPos.class);
	}

	@Override
	public void registerBaseMethods() {
		registerMethods(Block.class, BLOCK, Lists.newArrayList("getUnlocalizedName", "getMetaFromState", "getHarvestLevel", "isFoliage", "isWood", "canSustainLeaves"));
		registerMethods(Block.class, BLOCK, Lists.newArrayList("getWeakPower", "getStrongPower", "isSideSolid", "getBlockHardness"));
		registerMethods(BlockFluidBase.class, BLOCK, Lists.newArrayList("getFluid"));
		registerMethods(BlockCrops.class, BLOCK, Lists.newArrayList("isMaxAge"));
		registerMethods(Fluid.class, BLOCK, Lists.newArrayList("getLuminosity", "getDensity", "getTemperature", "getViscosity"));
		registerMethods(BlockPos.class, POS, Lists.newArrayList("getX", "getY", "getZ"));
		registerMethods(EnumFacing.class, FACE, Lists.newArrayList("toString"));
		registerMethods(World.class, WORLD, Lists.newArrayList("isBlockIndirectlyGettingPowered", "getWorldInfo"));
		registerMethods(WorldInfo.class, WORLD, Lists.newArrayList("isRaining", "isThundering", "getWorldName"));
		registerMethods(WorldProvider.class, WORLD, Lists.newArrayList("getDimension", "getDimensionType"));
		registerMethods(DimensionType.class, WORLD, Lists.newArrayList("getName"));
		registerMethods(Entity.class, ENTITY, Lists.newArrayList("getPosition", "getName"));
		registerMethods(EntityLivingBase.class, ENTITY, Lists.newArrayList("getHealth", "getMaxHealth", "getAge", "getTotalArmorValue"));
		registerMethods(EntityAgeable.class, ENTITY, Lists.newArrayList("getGrowingAge"));
		registerMethods(EntityPlayer.class, ENTITY, Lists.newArrayList("isCreative", "isSpectator", "getFoodStats", "getAbsorptionAmount", "getTeam", "getExperiencePoints"));
		registerMethods(FoodStats.class, ENTITY, Lists.newArrayList("getFoodLevel", "needFood", "getSaturationLevel"));
		registerMethods(Team.class, ENTITY, Lists.newArrayList("getRegisteredName"));
		registerMethods(ItemStack.class, ITEMSTACK, Lists.newArrayList("getItem", "getMaxStackSize", "getTagCompound"));
	}

	@Override
	public void registerAllFields() {
		HashMap<String, Integer> furnaceFields = Maps.<String, Integer>newHashMap();
		furnaceFields.put("furnaceBurnTime", 0);
		furnaceFields.put("currentItemBurnTime", 1);
		furnaceFields.put("cookTime", 2);
		furnaceFields.put("totalCookTime", 3);
		registerInvFields(TileEntityFurnace.class, furnaceFields);
		registerFields(TileEntityNote.class, TILE, Lists.newArrayList("note"));
		registerFields(ItemStack.class, ITEMSTACK, Lists.newArrayList("stackSize"));
	}

	@Override
	public void registerAdjustments() {
		registerInfoAdjustments(Lists.newArrayList("EntityLivingBase.getHealth", "EntityLivingBase.getMaxHealth"), "", "HP");
		registerInfoAdjustments(Lists.newArrayList("TileEntityFurnace.furnaceBurnTime", "TileEntityFurnace.currentItemBurnTime", "TileEntityFurnace.cookTime", "TileEntityFurnace.totalCookTime"), "", "ticks");
		// LogicRegistry.registerInfoAdjustments(Lists.newArrayList("Block.getUnlocalizedName"), "", ".name");
	}

}
