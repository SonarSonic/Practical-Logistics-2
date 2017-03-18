package sonar.logistics.guide.elements;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.recipes.ISonarRecipe;
import sonar.logistics.Logistics;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.IGuidePageElement;
import sonar.logistics.utils.HammerRecipes;

public class ElementHammerRecipe extends ElementRecipe<ISonarRecipe> implements IGuidePageElement {

	public static final ResourceLocation recipeB = new ResourceLocation(Logistics.MODID + ":textures/gui/forging_hammer_guide.png");

	public ElementHammerRecipe(int page, EntityPlayer player, ItemStack stack, int x, int y) {
		super(page, player, stack, x, y);
	}

	@Override
	public int[] getSizing() {
		return new int[] { x, y, 54, +18 };
	}

	@Override
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		if (recipe != null) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			
			GL11.glScaled(0.5, 0.5, 0.5);
			FontHelper.textCentre("Forging Hammer", (int)(56*2) + x + 2, (int)(y*2) + 2, LogisticsColours.white_text.getRGB());
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
					IGuidePageElement.renderItem(gui, stack, x + 1 + xy[0], y + 9 + 1 + xy[1]);
				}
			}
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY) {
		RenderHelper.saveBlendState();
		gui.drawTransparentRect(x, y, x + 54, y + 27, LogisticsColours.blue_overlay.getRGB());
		RenderHelper.restoreBlendState();
	}

	@Override
	public ISonarRecipe getRecipe() {
		//ISonarRecipe recipe = HammerRecipes.instance().getRecipeFromOutputs(player, new Object[] { stack });
		for(ISonarRecipe recipe : HammerRecipes.instance().getRecipes()){
			if(recipe.outputs().get(0).getJEIValue().get(0).isItemEqual(stack)){
				this.recipe = recipe;
			}
		}
		
		if(recipe!=null){
			stacks.set(0, recipe.inputs().get(0).getJEIValue());
			stacks.set(1, recipe.outputs().get(0).getJEIValue());
		}
		return recipe;
	}

	@Override
	public int[][] setSlots() {
		return new int[][] { new int[] { 0, 0 }, new int[] { 36, 0 } };
	}

	@Override
	public int recipeSize() {
		return 2;
	}

}
