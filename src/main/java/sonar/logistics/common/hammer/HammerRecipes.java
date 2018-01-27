package sonar.logistics.common.hammer;

import net.minecraft.item.ItemStack;
import sonar.core.recipes.DefinedRecipeHelper;
import sonar.core.recipes.ISonarRecipe;
import sonar.core.recipes.RecipeOreStack;
import sonar.logistics.PL2Items;

public class HammerRecipes extends DefinedRecipeHelper<ISonarRecipe> {

	private static final HammerRecipes instance = new HammerRecipes();

	public HammerRecipes() {
		super(1, 1, false);
	}

	public static final HammerRecipes instance() {
		return instance;
	}

	@Override
	public void addRecipes() {
		addRecipe("gemSapphire", new ItemStack(PL2Items.sapphire_dust));
		addRecipe("oreSapphire", new ItemStack(PL2Items.sapphire_dust, 2));
		addRecipe("stone", new ItemStack(PL2Items.stone_plate, 4));
		addRecipe(new RecipeOreStack("gemDiamond", 1), new ItemStack(PL2Items.etched_plate, 4));
		addRecipe(new RecipeOreStack("dustRedstone", 1), new ItemStack(PL2Items.signalling_plate, 4));
		addRecipe(new RecipeOreStack("enderpearl", 1), new ItemStack(PL2Items.wireless_plate, 4));
	}

	@Override
	public String getRecipeID() {
		return "forginghammer";
	}

}
