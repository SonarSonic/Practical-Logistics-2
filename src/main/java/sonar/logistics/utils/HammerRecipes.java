package sonar.logistics.utils;

import net.minecraft.item.ItemStack;
import sonar.core.recipes.DefinedRecipeHelper;
import sonar.core.recipes.ISonarRecipe;
import sonar.core.recipes.RecipeOreStack;
import sonar.logistics.LogisticsItems;

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
		this.addRecipe("gemSapphire", new ItemStack(LogisticsItems.sapphire_dust));
		this.addRecipe("stone", new ItemStack(LogisticsItems.stone_plate, 4));
		this.addRecipe("oreSapphire", new ItemStack(LogisticsItems.sapphire_dust, 2));
		this.addRecipe(new RecipeOreStack("gemDiamond", 4), new ItemStack(LogisticsItems.etched_plate, 1));
	}

	@Override
	public String getRecipeID() {
		return "forginghammer";
	}

}
