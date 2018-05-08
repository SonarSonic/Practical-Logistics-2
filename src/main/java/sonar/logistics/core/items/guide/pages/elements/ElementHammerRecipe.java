package sonar.logistics.core.items.guide.pages.elements;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.recipes.ISonarRecipe;
import sonar.logistics.PL2Constants;
import sonar.logistics.PL2Translate;
import sonar.logistics.base.gui.PL2Colours;
import sonar.logistics.core.items.guide.GuiGuide;
import sonar.logistics.core.tiles.misc.hammer.HammerRecipes;

import java.util.List;

public class ElementHammerRecipe extends ElementRecipe<ISonarRecipe> implements IGuidePageElement {

	public static final ResourceLocation recipeB = new ResourceLocation(PL2Constants.MODID + ":textures/gui/forging_hammer_guide.png");

	public ElementHammerRecipe(int page, EntityPlayer player, ItemStack stack, int x, int y) {
		super(page, player, stack, x, y);
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y, 80, +18 };
	}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		if (recipe != null) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();

			FontHelper.textCentre(PL2Translate.HAMMER.t(), x + 84, y + 26, PL2Colours.white_text.getRGB());
			GL11.glScaled(1 / 0.75, 1 / 0.75, 1 / 0.75);
			GL11.glScaled(0.5, 0.5, 0.5);
			gui.mc.getTextureManager().bindTexture(recipeB);
			gui.drawTexturedModalRect(x * 2, y * 2 + 18, 0, 0, 116, 18 * 2);
			GlStateManager.scale(1 / 0.5, 1 / 0.5, 1 / 0.5);

			int pos = 0;
			getPos();
			for (int i = 0; i < 2; i++) {
				int[] xy = slots[i];
				List<ItemStack> list = stacks.get(i);
				if (!list.isEmpty()) {
					Integer cyclePos = positions.get(i);
					ItemStack stack = list.get(cyclePos);
					IGuidePageElement.renderItem(gui, stack, x + 1 + (int)(xy[0]*0.75), y + 9 + 1 + (int)(xy[1]*0.75));
				}
			}
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		GuiSonar.drawTransparentRect(x, y + 24, x + 80, y + 64, PL2Colours.blue_overlay.getRGB());
	}

	@Override
	public ISonarRecipe getRecipe() {
		// ISonarRecipe recipe = HammerRecipes.instance().getRecipeFromOutputs(player, new Object[] { stack });
		for (ISonarRecipe recipe : HammerRecipes.instance().getRecipes()) {
			if (recipe.outputs().get(0).getJEIValue().get(0).isItemEqual(stack)) {
				this.recipe = recipe;
			}
		}

		if (recipe != null) {
			stacks.set(0, recipe.inputs().get(0).getJEIValue());
			stacks.set(1, recipe.outputs().get(0).getJEIValue());
		}
		return recipe;
	}

	@Override
	public int[][] setSlots() {
		return new int[][] { new int[] { 0, 0 }, new int[] { slotSize*2, 0 } };
	}

	@Override
	public int recipeSize() {
		return 2;
	}

}
