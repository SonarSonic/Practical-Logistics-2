package sonar.logistics;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import sonar.core.SonarCrafting;

public class PL2Crafting extends PL2 {

	public static void addRecipes() {

		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Items.guide, 1), "gemSapphire", Items.BOOK);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.data_cable, 16), "PPP", "DDD", "PPP", 'P', PL2Items.stone_plate, 'D', "dustSapphire");
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.redstone_cable, 16), "SSS", "DDD", "SSS", 'D', "dustRedstone", 'S', PL2Items.signalling_plate);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.redstone_node, 1), "   ", " C ", "PDP", 'P', PL2Items.signalling_plate, 'D', "dustRedstone", 'C', PL2Blocks.redstone_cable);
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Blocks.display_screen, 1), PL2Items.stone_plate, "dustSapphire", PL2Blocks.data_cable);
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Blocks.large_display_screen, 1), PL2Items.stone_plate, "dustSapphire", PL2Blocks.display_screen);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.holographic_display, 1), "   ", "PDP", " C ", 'P', PL2Items.stone_plate, 'D', PL2Blocks.display_screen, 'C', PL2Blocks.data_cable);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.redstone_signaller, 1), " T ", " R ", "PCP", 'P', PL2Items.stone_plate, 'R', PL2Items.signalling_plate, 'T', Blocks.REDSTONE_TORCH, 'C', PL2Blocks.data_cable);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.data_emitter, 1), "DPD", "PCP", "DDD", 'P', PL2Items.sapphire_dust, 'D', PL2Items.stone_plate, 'C', PL2Items.wireless_plate);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.data_receiver, 1), "DPD", "PCP", "DDD", 'P', PL2Items.sapphire_dust, 'D', PL2Items.stone_plate, 'C', PL2Blocks.info_reader);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.redstone_emitter, 1), "DPD", "PCP", "DDD", 'P', "dustRedstone", 'D', PL2Items.stone_plate, 'C', PL2Items.signalling_plate);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.redstone_receiver, 1), "DPD", "PCP", "DDD", 'P', "dustRedstone", 'D', PL2Items.stone_plate, 'C', PL2Blocks.info_reader);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.info_reader, 1), "PIP", "RDS", "PIP", 'R', "dustRedstone", 'I', "ingotIron", 'P', PL2Items.stone_plate, 'D', PL2Blocks.data_cable, 'S', "dustSapphire");
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Blocks.inventory_reader, 1), PL2Blocks.info_reader, "chestWood");
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Blocks.fluid_reader, 1), PL2Blocks.info_reader, Items.BUCKET);
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Blocks.energy_reader, 1), PL2Blocks.info_reader, "gemSapphire");
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Blocks.network_reader, 1), PL2Blocks.info_reader, PL2Items.signalling_plate);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.node, 1), "   ", " C ", "PDP", 'P', PL2Items.stone_plate, 'D', "dustSapphire", 'C', PL2Blocks.data_cable);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.transfer_node, 1), "   ", " C ", "PDP", 'P', PL2Items.stone_plate, 'D', PL2Items.etched_plate, 'C', PL2Blocks.node);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.hammer, 1), "ADA", "B B", "ACA", 'A', "logWood", 'B', "stickWood", 'C', "stone", 'D', "slabWood");
		SonarCrafting.addShapelessOre(PL2Constants.MODID, new ItemStack(PL2Blocks.entity_node, 1), PL2Items.wireless_plate, "gemSapphire", PL2Blocks.data_cable);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.clock, 1), "   ", "DCR", "PSP", 'P', PL2Items.stone_plate, 'S', PL2Items.signalling_plate, 'D', "dustSapphire", 'C', Items.CLOCK, 'R', "dustRedstone");
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Blocks.array, 1), "PPP", "RCE", "   ", 'P', PL2Items.stone_plate, 'C', PL2Blocks.data_cable, 'E', PL2Blocks.data_emitter, 'R', PL2Blocks.data_receiver);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.transceiver, 1), "SPD", "WBW", "SPD", 'P', PL2Items.stone_plate, 'B', PL2Blocks.node, 'S', "dustSapphire", 'D', "dustRedstone", 'W', PL2Items.wireless_plate);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.operator, 1), "  A", " B ", "C  ", 'A', "gemSapphire", 'B', "stickWood", 'C', "dustSapphire");
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.wireless_storage_reader, 1), "ABC", "DEF", "GBH", 'A', PL2Blocks.inventory_reader, 'B', PL2Items.wireless_plate, 'C', PL2Blocks.fluid_reader, 'D', PL2Items.transceiver, 'E', PL2Items.etched_plate, 'F', PL2Items.entity_transceiver, 'G', PL2Blocks.info_reader, 'H', PL2Blocks.energy_reader);
		SonarCrafting.addShapedOre(PL2Constants.MODID, new ItemStack(PL2Items.entity_transceiver, 1), "SPD", "WBW", "SPD", 'P', PL2Items.stone_plate, 'B', PL2Blocks.entity_node, 'S', "dustSapphire", 'D', "dustRedstone", 'W', PL2Items.wireless_plate);

	}
}