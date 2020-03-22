package sonar.logistics.base.data.methods;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import sonar.logistics.base.data.api.categories.DataCategories;
import sonar.logistics.base.data.api.methods.IMethod;

public class VanillaMethods {

    public static IMethod<Integer> FURNACE_BURN_TIME;
    public static IMethod<Integer> FURNACE_CURRENT_ITEM_BURN_TIME;
    public static IMethod<Integer> FURNACE_COOK_TIME;
    public static IMethod<Integer> FURNACE_TOTAL_COOK_TIME;

    public static IMethod<Integer> BLOCK_POS_X;
    public static IMethod<Integer> BLOCK_POS_Y;
    public static IMethod<Integer> BLOCK_POS_Z;
    public static IMethod<String> BLOCK_FACE;
    public static IMethod<String> BLOCK_UNLOCALIZED_NAME;
    public static IMethod<Integer> BLOCK_METADATA_FROM_STATE;
    public static IMethod<Float> BLOCK_HARDNESS;
    public static IMethod<Integer> BLOCK_HARVEST_LEVEL;
    public static IMethod<Boolean> BLOCK_IS_FOLIAGE;
    public static IMethod<Boolean> BLOCK_IS_WOOD;
    public static IMethod<Boolean> BLOCK_CAN_SUSTAIN_LEAVES;
    public static IMethod<Integer> BLOCK_WEAK_POWER;
    public static IMethod<Integer> BLOCK_STRONG_POWER;
    public static IMethod<Integer> BLOCK_INDIRECTLY_POWERED;
    public static IMethod<Boolean> BLOCK_IS_SIDE_SOLID;


    public static IMethod<Boolean> BLOCK_IS_MAX_AGE;

    public static IMethod<Integer> BLOCK_FLUID_LUMINOSITY;
    public static IMethod<Integer> BLOCK_FLUID_DENSITY;
    public static IMethod<Integer> BLOCK_FLUID_TEMPERATURE;
    public static IMethod<Integer> BLOCK_FLUID_VISCOSITY;
    public static IMethod<Boolean> BLOCK_FLUID_IS_GASEOUS;
    public static IMethod<String> BLOCK_FLUID_RARITY;

    public static IMethod<Boolean> WORLD_IS_RAINING;
    public static IMethod<Boolean> WORLD_IS_THUNDERING;
    public static IMethod<String> WORLD_NAME;
    public static IMethod<Integer> WORLD_DIMENSION;
    public static IMethod<String> WORLD_DIMENSION_NAME;

    public static IMethod<Integer> ENTITY_POS_X;
    public static IMethod<Integer> ENTITY_POS_Y;
    public static IMethod<Integer> ENTITY_POS_Z;
    public static IMethod<String> ENTITY_NAME;
    public static IMethod<Float> ENTITY_HEALTH;
    public static IMethod<Float> ENTITY_MAX_HEALTH;
    public static IMethod<Integer> ENTITY_TOTAL_ARMOR;

    public static IMethod<Boolean> PLAYER_IS_CREATIVE;
    public static IMethod<Boolean> PLAYER_IS_SPECTATOR;
    public static IMethod<Integer> PLAYER_FOOD_LEVEL;
    public static IMethod<Float> PLAYER_SATURATION_LEVEL;

    public static IMethod<IItemHandler> INVENTORY_CAPABILITY;
    public static IMethod<IEnergyStorage> ENERGY_CAPABILITY;
    public static IMethod<IFluidHandler> FLUID_CAPABILITY;

    public static void init(){
        FURNACE_BURN_TIME = MethodRegistry.registerMethodTileEntity(DataCategories.MACHINES,"furnace.burnTime", Integer.class, TileEntityFurnace.class, (E, T) -> T.getField(0));
        FURNACE_CURRENT_ITEM_BURN_TIME = MethodRegistry.registerMethodTileEntity(DataCategories.MACHINES,"furnace.itemBurnTime", Integer.class, TileEntityFurnace.class, (E, T) -> T.getField(1));
        FURNACE_COOK_TIME = MethodRegistry.registerMethodTileEntity( DataCategories.MACHINES,"furnace.cookTime", Integer.class, TileEntityFurnace.class, (E, T) -> T.getField(2));
        FURNACE_TOTAL_COOK_TIME = MethodRegistry.registerMethodTileEntity(DataCategories.MACHINES,"furnace.totalCookTime", Integer.class, TileEntityFurnace.class, (E, T) -> T.getField(3));

        BLOCK_UNLOCALIZED_NAME = MethodRegistry.registerMethodBlock(DataCategories.BLOCKS,"getUnlocalizedName", String.class, Block.class, (E, B) -> B.getUnlocalizedName());
        BLOCK_METADATA_FROM_STATE = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"getMetaFromState", Integer.class, Block.class, (E, B) -> B.getMetaFromState(E.state()));
        BLOCK_POS_X = MethodRegistry.registerMethodBlock(DataCategories.BLOCKS,"getX", Integer.class, Block.class, (E, B) -> E.pos().getX());
        BLOCK_POS_Y = MethodRegistry.registerMethodBlock(DataCategories.BLOCKS,"getY", Integer.class, Block.class, (E, B) -> E.pos().getY());
        BLOCK_POS_Z = MethodRegistry.registerMethodBlock(DataCategories.BLOCKS,"getZ", Integer.class, Block.class, (E, B) -> E.pos().getZ());
        BLOCK_FACE = MethodRegistry.registerMethodBlock(DataCategories.BLOCKS,"getFace", String.class, Block.class, (E, B) -> E.face().name());
        BLOCK_HARDNESS = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"getBlockHardness", Float.class, Block.class, (E, B) -> B.getBlockHardness(E.state(), E.world(), E.pos()));
        BLOCK_HARVEST_LEVEL = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"getHarvestLevel", Integer.class, Block.class, (E, B) -> B.getHarvestLevel(E.state()));
        BLOCK_IS_FOLIAGE = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"isFoliage", Boolean.class, Block.class, (E, B) -> B.isFoliage(E.world(), E.pos()));
        BLOCK_IS_WOOD = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"isWoord", Boolean.class, Block.class, (E, B) -> B.isWood(E.world(), E.pos()));
        BLOCK_CAN_SUSTAIN_LEAVES = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"canSustainLeaves", Boolean.class, Block.class, (E, B) -> B.canSustainLeaves(E.state(), E.world(), E.pos()));
        BLOCK_WEAK_POWER = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"getWeakPower", Integer.class, Block.class, (E, B) -> B.getWeakPower(E.state(), E.world(), E.pos(), E.face()));
        BLOCK_STRONG_POWER = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"getStrongPower", Integer.class, Block.class, (E, B) -> B.getStrongPower(E.state(), E.world(), E.pos(), E.face()));
        BLOCK_INDIRECTLY_POWERED = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"isBlockIndirectlyGettingPowered", Integer.class, Block.class, (E, B) -> E.world().isBlockIndirectlyGettingPowered(E.pos()));
        BLOCK_IS_SIDE_SOLID = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"isSideSolid", Boolean.class, Block.class, (E, B) -> B.isSideSolid(E.state(), E.world(), E.pos(), E.face()));

        BLOCK_IS_MAX_AGE = MethodRegistry.registerMethodBlock( DataCategories.BLOCKS,"isMaxAge", Boolean.class, BlockCrops.class, (E, B) -> B.isMaxAge(E.state()));

        BLOCK_FLUID_LUMINOSITY = MethodRegistry.registerMethodBlock(DataCategories.FLUIDS,"getLuminosity", Integer.class, BlockFluidBase.class, (E, B) -> B.getFluid().getLuminosity());
        BLOCK_FLUID_DENSITY = MethodRegistry.registerMethodBlock( DataCategories.FLUIDS,"getDensity", Integer.class, BlockFluidBase.class, (E, B) -> B.getFluid().getDensity());
        BLOCK_FLUID_TEMPERATURE = MethodRegistry.registerMethodBlock( DataCategories.FLUIDS,"getTemperature", Integer.class, BlockFluidBase.class, (E, B) -> B.getFluid().getTemperature());
        BLOCK_FLUID_VISCOSITY = MethodRegistry.registerMethodBlock( DataCategories.FLUIDS,"getViscosity", Integer.class, BlockFluidBase.class, (E, B) -> B.getFluid().getViscosity());
        BLOCK_FLUID_IS_GASEOUS = MethodRegistry.registerMethodBlock( DataCategories.FLUIDS,"isGaseous", Boolean.class, BlockFluidBase.class, (E, B) -> B.getFluid().isGaseous());
        BLOCK_FLUID_RARITY = MethodRegistry.registerMethodBlock( DataCategories.FLUIDS,"getRarity", String.class, BlockFluidBase.class, (E, B) -> B.getFluid().getRarity().rarityName);

        WORLD_IS_RAINING = MethodRegistry.registerMethodWorld( DataCategories.WORLD,"isRaining", Boolean.class, W -> W.getWorldInfo().isRaining());
        WORLD_IS_THUNDERING = MethodRegistry.registerMethodWorld( DataCategories.WORLD,"isThundering", Boolean.class, W -> W.getWorldInfo().isThundering());
        WORLD_NAME = MethodRegistry.registerMethodWorld( DataCategories.WORLD,"getWorldName", String.class, W -> W.getWorldInfo().getWorldName());
        WORLD_DIMENSION = MethodRegistry.registerMethodWorld(DataCategories.WORLD,"getDimension", Integer.class, W -> W.provider.getDimension());
        WORLD_DIMENSION_NAME = MethodRegistry.registerMethodWorld(DataCategories.WORLD,"getName", String.class, W -> W.provider.getDimensionType().getName());

        ENTITY_POS_X = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getX", Integer.class, Entity.class, (E, B) -> B.getPosition().getX());
        ENTITY_POS_Y = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getY", Integer.class, Entity.class, (E, B) -> B.getPosition().getY());
        ENTITY_POS_Z = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getZ", Integer.class, Entity.class, (E, B) -> B.getPosition().getZ());
        ENTITY_NAME = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getName", String.class, Entity.class, (E, B) -> B.getName());
        ENTITY_HEALTH = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getHealth", Float.class, EntityLivingBase.class, (E, B) -> B.getHealth());
        ENTITY_MAX_HEALTH = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getMaxHealth", Float.class, EntityLivingBase.class, (E, B) -> B.getMaxHealth());
        ENTITY_TOTAL_ARMOR = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getTotalArmorValue", Integer.class, EntityLivingBase.class, (E, B) -> B.getTotalArmorValue());

        PLAYER_IS_CREATIVE = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"isCreative", Boolean.class, EntityPlayerMP.class, (E, P) -> P.isCreative());
        PLAYER_IS_SPECTATOR = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"isSpectator", Boolean.class, EntityPlayerMP.class, (E, P) -> P.isSpectator());
        PLAYER_FOOD_LEVEL = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getFoodStats", Integer.class, EntityPlayerMP.class, (E, P) -> P.getFoodStats().getFoodLevel());
        PLAYER_SATURATION_LEVEL = MethodRegistry.registerMethodEntity(DataCategories.ENTITIES,"getFoodStats", Float.class, EntityPlayerMP.class, (E, P) -> P.getFoodStats().getSaturationLevel());

        INVENTORY_CAPABILITY = MethodRegistry.registerMethodTileEntity(DataCategories.INVENTORIES,"items", IItemHandler.class, TileEntity.class, (E, T) -> T.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, E.face()));
        ENERGY_CAPABILITY = MethodRegistry.registerMethodTileEntity(DataCategories.ENERGY,"storage", IEnergyStorage.class, TileEntity.class, (E, T) -> T.getCapability(CapabilityEnergy.ENERGY, E.face()));
        FLUID_CAPABILITY = MethodRegistry.registerMethodTileEntity(DataCategories.FLUIDS,"storage", IFluidHandler.class, TileEntity.class, (E, T) -> T.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, E.face()));

    }
}
