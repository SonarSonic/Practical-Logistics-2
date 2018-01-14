package sonar.logistics.integration;

import java.util.function.BiFunction;

import com.google.common.collect.Lists;

import appeng.api.recipes.IIngredient;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import sonar.core.integration.SonarLoader;
import sonar.core.integration.minetweaker.SonarAddRecipeV2;
import sonar.core.integration.minetweaker.SonarRemoveRecipeV2;
import sonar.core.recipes.ISonarRecipe;
import sonar.core.recipes.RecipeHelperV2;
import sonar.core.recipes.RecipeObjectType;
import sonar.logistics.common.hammer.HammerRecipes;
import sonar.logistics.integration.PracticalLogisticsJEI.Handlers;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

public class MineTweakerIntegration {

	public static void init() {
		CraftTweakerAPI.registerClass(HammerHandler.class);
	}

	public static Object createJEIRecipe(ISonarRecipe recipe, RecipeHelperV2<ISonarRecipe> helper) {
		if (SonarLoader.jeiLoaded()) {
			for (Handlers handler : PracticalLogisticsJEI.Handlers.values()) {
				if (handler.helper.getRecipeID().equals(helper.getRecipeID())) {
					try {
						return handler.recipeClass.getConstructor(RecipeHelperV2.class, ISonarRecipe.class).newInstance(handler.helper, recipe);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	@ZenClass("mods.logistics.hammer")
	public static class HammerHandler {

		@ZenMethod
		public static void addRecipe(IIngredient input1, IItemStack output) {
			CraftTweakerAPI.apply(new SonarAddRecipeV2(HammerRecipes.instance(), Lists.newArrayList(input1), Lists.newArrayList(CraftTweakerMC.getItemStack(output))));
		}

		@ZenMethod
		public static void removeRecipe(IIngredient output) {
			CraftTweakerAPI.apply(new SonarRemoveRecipeV2(HammerRecipes.instance(), RecipeObjectType.OUTPUT, Lists.newArrayList(output)));
		}
	}
}