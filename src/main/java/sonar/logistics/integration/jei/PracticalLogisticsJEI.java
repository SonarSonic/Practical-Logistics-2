package sonar.logistics.integration.jei;
/*
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.item.ItemStack;
import sonar.core.integration.jei.JEISonarPlugin;
import sonar.core.integration.jei.JEISonarProvider;
import sonar.logistics.PL2Blocks;
import sonar.logistics.PL2Constants;
import sonar.logistics.core.tiles.misc.hammer.ContainerHammer;
import sonar.logistics.core.tiles.misc.hammer.GuiHammer;
import sonar.logistics.core.tiles.misc.hammer.HammerRecipes;

@JEIPlugin
public class PracticalLogisticsJEI extends JEISonarPlugin {

    private JEISonarProvider HAMMER;

    @Override
    public void registerProviders() {
        HAMMER = p(HammerRecipes.instance(), PL2Blocks.hammer, ForgingHammerJEI.Hammer.class, ForgingHammerJEI.Hammer::new, ForgingHammerJEI::new, "hammer", PL2Constants.MODID);
    }

    @Override
    public void register(IModRegistry registry) {
        super.register(registry);
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(PL2Blocks.hammer_air));

        registry.addRecipeClickArea(GuiHammer.class, 79, 26, 18, 8, HAMMER.getID());
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerHammer.class, HAMMER.getID(), 0, 1, 2, 36);
    }

}
*/