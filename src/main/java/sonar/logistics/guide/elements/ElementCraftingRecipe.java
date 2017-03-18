package sonar.logistics.guide.elements;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.recipes.RecipeUtils;
import sonar.logistics.Logistics;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.IGuidePageElement;

public class ElementCraftingRecipe extends ElementRecipe<IRecipe> implements IGuidePageElement {

	public static final ResourceLocation recipeB = new ResourceLocation(Logistics.MODID + ":textures/gui/crafting_recipe_guide.png");

	public ElementCraftingRecipe(int page, EntityPlayer player, ItemStack stack, int x, int y) {
		super(page, player, stack, x, y);
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y, (int) ((54) * 1 / 0.75), (int) ((60) * 1 / 0.75) + 24 };
	}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		if (recipe != null) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			GL11.glScaled(0.5, 0.5, 0.5);
			FontHelper.textCentre("Crafting Table", (int)(56*2) + x + 3, (int)(y*2) + 2, LogisticsColours.white_text.getRGB());
			gui.mc.getTextureManager().bindTexture(recipeB);
			gui.drawTexturedModalRect(x * 2, 12 + y * 2, 0, 0, 116, 54 * 2);
			GlStateManager.scale(1 / 0.5, 1 / 0.5, 1 / 0.5);
			RenderHelper.saveBlendState();
			gui.drawTransparentRect(x, y, x + 54, y + 60, LogisticsColours.blue_overlay.getRGB());
			RenderHelper.restoreBlendState();

			int pos = 0;
			getPos();
			for (int i = 0; i < 9; i++) {
				int[] xy = slots[i];
				List<ItemStack> list = stacks.get(i);
				if (!list.isEmpty()) {
					Integer cyclePos = positions.get(i);
					ItemStack stack = list.get(cyclePos);
					IGuidePageElement.renderItem(gui, stack, x + 1 + xy[0], 6 + y + 1 + xy[1]);
				}
			}
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		RenderHelper.saveBlendState();
		gui.drawTransparentRect(x, y, x + 54, y + 60, LogisticsColours.blue_overlay.getRGB());
		RenderHelper.restoreBlendState();
	}

	public IRecipe getRecipe() {
		for (IRecipe recipe : CraftingManager.getInstance().getRecipeList()) {
			if (ItemStack.areItemsEqual(recipe.getRecipeOutput(), stack)) {
				stacks = RecipeUtils.configureStacks(recipe);
				return recipe;
			}
		}
		return null;
	}

	@Override
	public int[][] setSlots() {
		return new int[][] { new int[] { 0, 0 }, new int[] { 18, 0 }, new int[] { 36, 0 }, new int[] { 0, 18 }, new int[] { 18, 18 }, new int[] { 36, 18 }, new int[] { 0, 36 }, new int[] { 18, 36 }, new int[] { 36, 36 } };

	}

	@Override
	public int recipeSize() {
		return 9;
	}

}
