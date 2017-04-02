package sonar.logistics.guide;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.item.ItemStack;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.guide.elements.ElementCraftingRecipe;
import sonar.logistics.guide.elements.ElementHammerRecipe;
import sonar.logistics.guide.elements.ElementItem;
import sonar.logistics.guide.elements.ElementLink;

public class BaseItemPage extends GeneralPage implements IGuidePage {

	public String unlocalizedName;
	public ItemStack stack;
	public ArrayList<ElementLink> links = new ArrayList();
	public double rotate = 0;

	public BaseItemPage(int pageID, ItemStack stack) {
		this(pageID, stack, new String[] { "guide." + stack.getUnlocalizedName().substring(5) + ".name" });
	}

	public BaseItemPage(int pageID, ItemStack stack, String... descKey) {
		super(pageID, "", descKey);
		this.stack = stack;
		this.unlocalizedName = stack.getUnlocalizedName();
	}

	public String getDisplayName() {
		return stack.getDisplayName();
	}

	public ItemStack getItemStack() {
		return stack;
	}

	//// CREATE \\\\
	
	public ArrayList<IGuidePageElement> getElements(GuiGuide gui, ArrayList<IGuidePageElement> elements) {
		super.getElements(gui, elements);
		elements.add(new ElementItem(0, stack, 4, 15));
		ElementCraftingRecipe recipe = new ElementCraftingRecipe(0, gui.mc.thePlayer, stack, 4, 74);
		if (recipe.recipe != null) {
			elements.add(recipe);
		} else {
			ElementHammerRecipe hammerR = new ElementHammerRecipe(0, gui.mc.thePlayer, stack, 4, 74);
			if (hammerR.recipe != null) {
				elements.add(hammerR);
			}
		}

		return elements;
	}

	//// DRAWING \\\\

	public void drawPageInGui(GuiGuide gui, int yPos) {
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.saveBlendState();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.renderItem(gui, 8, yPos - 1, stack);
		RenderHelper.renderStoredItemStackOverlay(stack, stack.stackSize == 1 ? 0 : stack.stackSize, 8, yPos - 1, null, true);
		RenderHelper.restoreBlendState();
		FontHelper.text(stack.getDisplayName(), 28, yPos + 3, -1);
	}
}