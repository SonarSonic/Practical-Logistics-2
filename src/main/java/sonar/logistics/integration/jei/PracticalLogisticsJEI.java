package sonar.logistics.integration.jei;

import mezz.jei.api.*;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import sonar.core.handlers.inventories.ItemStackHelper;
import sonar.core.integration.jei.IJEIHandler;
import sonar.core.integration.jei.JEICategoryV2;
import sonar.core.integration.jei.JEIHelper;
import sonar.core.integration.jei.JEIRecipeV2;
import sonar.core.recipes.IRecipeHelperV2;
import sonar.logistics.PL2;
import sonar.logistics.PL2Blocks;
import sonar.logistics.core.tiles.misc.hammer.ContainerHammer;
import sonar.logistics.core.tiles.misc.hammer.GuiHammer;
import sonar.logistics.core.tiles.misc.hammer.HammerRecipes;

import java.util.List;

@JEIPlugin
public class PracticalLogisticsJEI implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		PL2.logger.info("Starting JEI Integration");
		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		for (IJEIHandler handler : Handlers.values()) {
			registry.addRecipes(handler.getJEIRecipes());
			JEICategoryV2 cat = handler.getCategory(guiHelper);
			registry.addRecipeCategories(cat);
			registry.addRecipeHandlers(cat);
			if (handler.getCrafterItemStack() != null)
				registry.addRecipeCatalyst(handler.getCrafterItemStack(), handler.getUUID());

			PL2.logger.info("Registering Recipe Handler: " + handler.getUUID());
		}
		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
		//registry.addRecipeCategoryCraftingItem(new ItemStack(LogisticsBlocks.hammer, 1), Handlers.HAMMER.getUUID());
		registry.addRecipeClickArea(GuiHammer.class, 79, 26, 18, 8, Handlers.HAMMER.getUUID());
		recipeTransferRegistry.addRecipeTransferHandler(ContainerHammer.class, Handlers.HAMMER.getUUID(), 0, 1, 2, 36);

		PL2.logger.info("Finished JEI Integration");
	}

	public enum Handlers implements IJEIHandler {
		HAMMER(HammerRecipes.instance(), PL2Blocks.hammer, "hammer", ForgingHammerJEI.Hammer.class);

		/**/
		public IRecipeHelperV2 helper;
		public String unlocalizedName;
		public String textureName;
		public Class<? extends JEIRecipeV2> recipeClass;
		public ItemStack crafter;

		Handlers(IRecipeHelperV2 helper, Object stack, String textureName, Class<? extends JEIRecipeV2> recipeClass) {
			this.helper = helper;
			this.crafter = ItemStackHelper.getOrCreateStack(stack);
			this.unlocalizedName = crafter.getUnlocalizedName() + ".name";
			this.textureName = textureName;
			this.recipeClass = recipeClass;
		}

		@Override
		public JEICategoryV2 getCategory(IGuiHelper guiHelper) {
			switch (this) {
			case HAMMER:
				return new ForgingHammerJEI(guiHelper, this);
			default:
				return null;
			}
		}

		@Override
		public String getTextureName() {
			return textureName;
		}

		@Override
		public String getTitle() {
			return unlocalizedName;
		}

		@Override
		public Class<? extends JEIRecipeV2> getRecipeClass() {
			return recipeClass;
		}

		@Override
		public IRecipeHelperV2 getRecipeHelper() {
			return helper;
		}

		public List<JEIRecipeV2> getJEIRecipes() {
			return JEIHelper.getJEIRecipes(helper, recipeClass);
		}

		@Override
		public ItemStack getCrafterItemStack() {
			return crafter;
		}

		@Override
		public String getUUID() {
			return helper.getRecipeID();
		}
	}
}
