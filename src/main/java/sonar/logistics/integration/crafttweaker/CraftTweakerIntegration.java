package sonar.logistics.integration.crafttweaker;

import com.google.common.collect.Lists;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import sonar.core.integration.crafttweaker.SonarAddRecipe;
import sonar.core.integration.crafttweaker.SonarRemoveRecipe;
import sonar.core.recipes.RecipeObjectType;
import sonar.logistics.core.tiles.misc.hammer.HammerRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

public class CraftTweakerIntegration {

	@ZenClass("mods.logistics.hammer")
	@ZenRegister
	public static class HammerHandler {

		@ZenMethod
		public static void addRecipe(IIngredient input1, IIngredient output) {
			CraftTweakerAPI.apply(new SonarAddRecipe(HammerRecipes.instance(), Lists.newArrayList(input1), Lists.newArrayList(output)));
		}

		@ZenMethod
		public static void removeRecipe(IIngredient output) {
			CraftTweakerAPI.apply(new SonarRemoveRecipe(HammerRecipes.instance(), RecipeObjectType.OUTPUT, Lists.newArrayList(output)));
		}
	}
}
