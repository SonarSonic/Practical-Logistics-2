package sonar.logistics.guide;

import org.lwjgl.opengl.GL11;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.client.gui.GuiGuide;

public interface IGuidePageElement {

	public int getDisplayPage();
	/**left, top, width, height*/
	public int[] getSizing();

	public void drawElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY);
	
	public void drawForegroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY);
	
	public void drawBackgroundElement(GuiGuide gui, int x, int y, int page, int mouseX, int mouseY);
	
	public boolean mouseClicked(GuiGuide gui, IGuidePage page, int x, int y, int button);	

	public static void renderItem(GuiGuide gui, ItemStack stack, int xPos, int yPos) {
		ItemStack rendStack = stack.copy();
		if (rendStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
			rendStack.setItemDamage(0);
		}
		RenderHelper.saveBlendState();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.renderItem(gui, xPos, yPos, rendStack);
		RenderHelper.renderStoredItemStackOverlay(rendStack, rendStack.getCount() == 1 ? 0 : rendStack.getCount(), xPos, yPos, null, true);
		RenderHelper.restoreBlendState();
	}
}
