package sonar.logistics.integration;

import java.util.function.BiFunction;

import com.google.common.collect.Lists;

import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
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
		MineTweakerAPI.registerClass(HammerHandler.class);
	}

	public static final BiFunction<ISonarRecipe, RecipeHelperV2<ISonarRecipe>, Object> createRecipe = ((a, b) -> createJEIRecipe(a, b));

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
			MineTweakerAPI.apply(new SonarAddRecipeV2(HammerRecipes.instance(), Lists.newArrayList(input1), Lists.newArrayList(MineTweakerMC.getItemStack(output)),createRecipe));
		}

		@ZenMethod
		public static void removeRecipe(IIngredient output) {
			MineTweakerAPI.apply(new SonarRemoveRecipeV2(HammerRecipes.instance(), RecipeObjectType.OUTPUT, Lists.newArrayList(output), createRecipe));
		}
	}
}