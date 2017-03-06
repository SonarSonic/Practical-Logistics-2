package sonar.logistics;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class LogisticsCrafting extends Logistics {

	
	
	public static void addRecipes() {
		addShapedOre(new ItemStack(LogisticsItems.partCable, 16), new Object[] { "PPP", "DDD", "PPP", 'P', LogisticsItems.stone_plate, 'D', "dustSapphire" });
		addShapelessOre(new ItemStack(LogisticsItems.displayScreen, 1), new Object[] { LogisticsItems.stone_plate, "dustSapphire", LogisticsItems.partCable });
		addShapelessOre(new ItemStack(LogisticsItems.largeDisplayScreen, 1), new Object[] { LogisticsItems.stone_plate, "dustSapphire", LogisticsItems.displayScreen });
		
		addShapedOre(new ItemStack(LogisticsItems.holographicDisplay, 1), new Object[] { "   ", "PDP", " C ", 'P', LogisticsItems.stone_plate, 'D', LogisticsItems.displayScreen, 'C', LogisticsItems.partCable });
		//addShapedOre(new ItemStack(LogisticsBlocks.dataModifier, 1), new Object[] { "DCD", "PDC", "DCD", 'P', LogisticsItems.stone_plate, 'D', "dustSapphire", 'C', LogisticsItems.partCable });
		//addShapedOre(new ItemStack(LogisticsBlocks.infoCreator, 1), new Object[] { "DPD", "PCD", "DPD", 'P', LogisticsItems.stone_plate, 'D', "dustSapphire", 'C', LogisticsItems.partCable });
		
		addShapedOre(new ItemStack(LogisticsItems.partRedstoneSignaller, 1), new Object[] { "P  ", "CT ", "PPP", 'P', LogisticsItems.stone_plate, 'T', Blocks.REDSTONE_TORCH, 'C', LogisticsItems.partCable });
		addShapedOre(new ItemStack(LogisticsItems.partEmitter, 1), new Object[] { "DPD", "PCP", "DDD", 'P', "dustRedstone", 'D', LogisticsItems.stone_plate, 'C', Items.ENDER_PEARL });
		addShapedOre(new ItemStack(LogisticsItems.partReceiver, 1), new Object[] { "DPD", "PCP", "DDD", 'P', "dustRedstone", 'D', LogisticsItems.stone_plate, 'C', LogisticsItems.infoReaderPart });
		addShapedOre(new ItemStack(LogisticsItems.infoReaderPart, 1), new Object[] { "PIP", "RDS", "PIP", 'R', "dustRedstone", 'I', Items.IRON_INGOT, 'P', LogisticsItems.stone_plate, 'D', LogisticsItems.partCable, 'S', "dustSapphire" });
		addShapelessOre(new ItemStack(LogisticsItems.inventoryReaderPart, 1), new Object[] { LogisticsItems.infoReaderPart, Blocks.CHEST });
		addShapelessOre(new ItemStack(LogisticsItems.fluidReaderPart, 1), new Object[] { LogisticsItems.infoReaderPart, Items.BUCKET });
		addShapelessOre(new ItemStack(LogisticsItems.energyReaderPart, 1), new Object[] { LogisticsItems.infoReaderPart, "gemSapphire" });
		addShapedOre(new ItemStack(LogisticsItems.partNode, 1), new Object[] { "   ", " C ", "PDP", 'P', LogisticsItems.stone_plate, 'D', "dustSapphire", 'C', LogisticsItems.partCable });
		addShapedOre(new ItemStack(LogisticsItems.partTransferNode, 1), new Object[] { "   ", " C ", "PDP", 'P', LogisticsItems.stone_plate, 'D', "dustSapphire", 'C', LogisticsItems.partNode });
		addShapedOre(new ItemStack(LogisticsBlocks.hammer, 1), new Object[] { "ADA", "B B", "ACA", 'A', "logWood", 'B', "stickWood", 'C', "stone", 'D', "slabWood" });
		//addShapelessOre(new ItemStack(BlockRegistry.entityNode, 1), new Object[] { ItemRegistry.stone_plate, "gemSapphire", ItemRegistry.partCable });
		//addShapedOre(new ItemStack(LogisticsBlocks.itemRouter, 1), new Object[] { "SIS", "IMI", "SIS", 'S', "gemSapphire", 'I', LogisticsItems.inventoryReaderPart, 'M', LogisticsBlocks.dataModifier });
		//addShapedOre(new ItemStack(BlockRegistry.channelledCable, 6), new Object[] { "CCC", "SSS", "CCC", 'C', ItemRegistry.partCable, 'S', "dustSapphire" });
		//addShapedOre(new ItemStack(BlockRegistry.channelSelector, 1), new Object[] { "CDC", "PCD", "CDC", 'P', ItemRegistry.stone_plate, 'D', "dustSapphire", 'C', BlockRegistry.channelledCable });
		addShapedOre(new ItemStack(LogisticsItems.partClock, 1), new Object[] { "   ", "DCR", "PPP", 'P', LogisticsItems.stone_plate, 'D', "dustSapphire", 'C', Items.CLOCK, 'R', "dustRedstone" });
		addShapedOre(new ItemStack(LogisticsItems.partArray, 1), new Object[] { "PPP", "RCE", "   ", 'P', LogisticsItems.stone_plate, 'C', LogisticsItems.partCable, 'E', LogisticsItems.partEmitter, 'R', LogisticsItems.partReceiver });
		addShapedOre(new ItemStack(LogisticsItems.transceiver, 1), new Object[] { "SPD", "RBE", "SPD", 'P', LogisticsItems.stone_plate, 'B', Items.ENDER_PEARL, 'S', "dustSapphire", 'D', "dustRedstone", 'E', LogisticsItems.partEmitter, 'R', LogisticsItems.partReceiver });
		addShapedOre(new ItemStack(LogisticsItems.operator, 1), new Object[] { "  A", " B ", "C  ", 'A', "gemSapphire", 'B', "stickWood", 'C', "dustSapphire" });
	}


	public static void addShaped(ItemStack result, Object... input) {
		if (result != null && result.getItem() != null && input != null) {
			try {
				GameRegistry.addRecipe(result, input);
			} catch (Exception exception) {
				logger.error("ERROR ADDING SHAPED RECIPE: " + result);
			}
		}
	}

	public static void addShapedOre(ItemStack result, Object... input) {
		if (result != null && result.getItem() != null && input != null) {
			try {
				ShapedOreRecipe oreRecipe = new ShapedOreRecipe(result, input);
				GameRegistry.addRecipe(oreRecipe);
			} catch (Exception exception) {
				logger.error("ERROR ADDING SHAPED ORE RECIPE: " + result);
			}
		}
	}

	public static void addShapeless(ItemStack result, Object... input) {
		if (result != null && result.getItem() != null && input != null) {
			try {
				GameRegistry.addShapelessRecipe(result, input);
			} catch (Exception exception) {
				logger.error("ERROR ADDING SHAPELESS RECIPE: " + result);
			}
		}
	}

	public static void addShapelessOre(ItemStack result, Object... input) {
		if (result != null && result.getItem() != null && input != null) {
			try {
				ShapelessOreRecipe oreRecipe = new ShapelessOreRecipe(result, input);
				GameRegistry.addRecipe(oreRecipe);
			} catch (Exception exception) {
				logger.error("ERROR ADDING SHAPED ORE RECIPE: " + result);
			}
		}
	}
}