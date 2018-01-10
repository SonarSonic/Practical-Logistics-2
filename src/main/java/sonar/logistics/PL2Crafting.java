package sonar.logistics;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import sonar.core.SonarCrafting;

public class PL2Crafting extends PL2 {

	public static void addRecipes() {
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.guide, 1), new Object[] { "gemSapphire", Items.BOOK });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.cable, 16), new Object[] { "PPP", "DDD", "PPP", 'P', PL2Items.stone_plate, 'D', "dustSapphire" });
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.display_screen, 1), new Object[] { PL2Items.stone_plate, "dustSapphire", PL2Items.cable });
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.large_display_screen, 1), new Object[] { PL2Items.stone_plate, "dustSapphire", PL2Items.display_screen });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.holographic_display, 1), new Object[] { "   ", "PDP", " C ", 'P', PL2Items.stone_plate, 'D', PL2Items.display_screen, 'C', PL2Items.cable });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.redstone_signaller, 1), new Object[] { "P  ", "CT ", "PPP", 'P', PL2Items.stone_plate, 'T', Blocks.REDSTONE_TORCH, 'C', PL2Items.cable });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.data_emitter, 1), new Object[] { "DPD", "PCP", "DDD", 'P', "dustRedstone", 'D', PL2Items.stone_plate, 'C', Items.ENDER_PEARL });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.data_receiver, 1), new Object[] { "DPD", "PCP", "DDD", 'P', "dustRedstone", 'D', PL2Items.stone_plate, 'C', PL2Items.info_reader });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.info_reader, 1), new Object[] { "PIP", "RDS", "PIP", 'R', "dustRedstone", 'I', Items.IRON_INGOT, 'P', PL2Items.stone_plate, 'D', PL2Items.cable, 'S', "dustSapphire" });
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.inventory_reader, 1), new Object[] { PL2Items.info_reader, Blocks.CHEST });
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.fluid_reader, 1), new Object[] { PL2Items.info_reader, Items.BUCKET });
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.energy_reader, 1), new Object[] { PL2Items.info_reader, "gemSapphire" });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.node, 1), new Object[] { "   ", " C ", "PDP", 'P', PL2Items.stone_plate, 'D', "dustSapphire", 'C', PL2Items.cable });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.transfer_node, 1), new Object[] { "   ", " C ", "PDP", 'P', PL2Items.stone_plate, 'D', PL2Items.etched_plate, 'C', PL2Items.node });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.hammer, 1), new Object[] { "ADA", "B B", "ACA", 'A', "logWood", 'B', "stickWood", 'C', "stone", 'D', "slabWood" });
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.entity_node, 1), new Object[] { PL2Items.stone_plate, "gemSapphire", PL2Items.cable });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.clock, 1), new Object[] { "   ", "DCR", "PPP", 'P', PL2Items.stone_plate, 'D', "dustSapphire", 'C', Items.CLOCK, 'R', "dustRedstone" });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.array, 1), new Object[] { "PPP", "RCE", "   ", 'P', PL2Items.stone_plate, 'C', PL2Items.cable, 'E', PL2Items.data_emitter, 'R', PL2Items.data_receiver });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.transceiver, 1), new Object[] { "SPD", "RBE", "SPD", 'P', PL2Items.stone_plate, 'B', Items.ENDER_PEARL, 'S', "dustSapphire", 'D', "dustRedstone", 'E', PL2Items.data_emitter, 'R', PL2Items.data_receiver });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.operator, 1), new Object[] { "  A", " B ", "C  ", 'A', "gemSapphire", 'B', "stickWood", 'C', "dustSapphire" });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.wireless_storage_reader, 1), new Object[] { "ABC", "DEF", "GBH", 'A', PL2Items.inventory_reader, 'B', PL2Items.stone_plate, 'C', PL2Items.fluid_reader, 'D', PL2Items.transceiver, 'E', PL2Items.etched_plate, 'F', PL2Items.entity_transceiver, 'G', PL2Items.info_reader, 'H', PL2Items.energy_reader });
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.entity_transceiver, 1), new Object[] { "SPD", "RBE", "SPD", 'P', PL2Items.stone_plate, 'B', Items.ENDER_EYE, 'S', "dustSapphire", 'D', "dustRedstone", 'E', PL2Items.data_emitter, 'R', PL2Items.data_receiver });
	}
}