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
import sonar.logistics.PL2Constants;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.IGuidePageElement;

public class ElementCraftingRecipe extends ElementRecipe<IRecipe> implements IGuidePageElement {

	public static final ResourceLocation recipeB = new ResourceLocation(PL2Constants.MODID + ":textures/gui/crafting_recipe_guide.png");

	public ElementCraftingRecipe(int page, EntityPlayer player, ItemStack stack, int x, int y) {
		super(page, player, stack, x, y);
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y, 80, 80 + 16 };
	}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		if (recipe != null) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			FontHelper.textCentre("Crafting Table", x + 84, y + 2, LogisticsColours.white_text.getRGB());
			GL11.glScaled(1 / 0.75, 1 / 0.75, 1 / 0.75);
			GL11.glScaled(0.5, 0.5, 0.5);
			gui.mc.getTextureManager().bindTexture(recipeB);
			gui.drawTexturedModalRect(x + 8, y + 72, 0, 0, 120, 120);
			GlStateManager.scale(1 / 0.5, 1 / 0.5, 1 / 0.5);

			RenderHelper.saveBlendState();
			// gui.drawTransparentRect(x, y, x + (int)(80*0.75), y + (int)((80+24)*0.75), LogisticsColours.blue_overlay.getRGB());
			RenderHelper.restoreBlendState();

			int pos = 0;
			getPos();
			for (int i = 0; i < 9; i++) {
				int[] xy = slots[i];
				List<ItemStack> list = stacks.get(i);
				if (!list.isEmpty()) {
					Integer cyclePos = positions.get(i);
					ItemStack stack = list.get(cyclePos);
					IGuidePageElement.renderItem(gui, stack, x + (int)(xy[0]*0.75), y + (int)(xy[1]*0.75));
				}
			}
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		RenderHelper.saveBlendState();
		gui.drawTransparentRect(x, y, x + 80, y + 80 + 16, LogisticsColours.blue_overlay.getRGB());
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
		int xOffset = 4;
		int yOffset = -20;
		return new int[][] { new int[] { xOffset, yOffset }, new int[] { slotSize + xOffset, yOffset }, new int[] { slotSize * 2 + xOffset, yOffset }, new int[] { 0 + xOffset, slotSize + yOffset }, new int[] { slotSize + xOffset, slotSize + yOffset }, new int[] { slotSize * 2 + xOffset, slotSize + yOffset }, new int[] { 0 + xOffset, slotSize*2 + yOffset }, new int[] { slotSize + xOffset, slotSize * 2 + yOffset }, new int[] { slotSize * 2 + xOffset, slotSize * 2 + yOffset } };

	}

	@Override
	public int recipeSize() {
		return 9;
	}

}
