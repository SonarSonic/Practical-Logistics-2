package sonar.logistics.guide;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import mcmultipart.MCMultiPartMod;
import mcmultipart.block.TileMultipartContainer;
import mcmultipart.multipart.MultipartHelper;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import sonar.core.client.gui.GuiBlockRenderer3D;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.utils.Pair;
import sonar.logistics.api.cabling.CableConnection;
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.GuiGuide;
import sonar.logistics.common.multiparts.ArrayPart;
import sonar.logistics.common.multiparts.DataCablePart;
import sonar.logistics.common.multiparts.DisplayScreenPart;
import sonar.logistics.common.multiparts.InfoReaderPart;
import sonar.logistics.common.multiparts.NodePart;
import sonar.logistics.guide.elements.ElementItem;
import sonar.logistics.guide.elements.ElementCraftingRecipe;
import sonar.logistics.guide.elements.ElementHammerRecipe;
import sonar.logistics.guide.elements.ElementInfo;
import sonar.logistics.guide.elements.ElementLink;
import sonar.logistics.info.LogicInfoRegistry.RegistryType;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.ProgressInfo;

public abstract class BaseItemPage extends BaseInfoPage implements IGuidePage {

	public String unlocalizedName;
	public ItemStack stack;
	public ArrayList<ElementLink> links = new ArrayList();
	public double rotate = 0;

	public BaseItemPage(int pageID, ItemStack stack) {
		super(pageID);
		this.stack = stack;
		this.unlocalizedName = stack.getUnlocalizedName();
	}

	public ItemStack getItemStack() {
		return stack;
	}

	public void drawPageInGui(GuiGuide gui, int yPos) {
		RenderHelper.saveBlendState();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.renderItem(gui, 8, yPos - 1, stack);
		RenderHelper.renderStoredItemStackOverlay(stack, stack.stackSize == 1 ? 0 : stack.stackSize, 8, yPos - 1, null, true);
		RenderHelper.restoreBlendState();
		FontHelper.text(stack.getDisplayName(), 28, yPos + 3, -1);
	}

	public void drawForegroundPage(GuiGuide gui, int x, int y, int page) {
		super.drawForegroundPage(gui, x, y, page);
		/* GuidePageInfoFormatted current = pageInfo.get("guide." + unlocalizedName.substring(5) + ".name"); if (current != null) { FontHelper.text(current.formattedList, 0, 7, 12, 96, 25, LogisticsColours.white_text.getRGB()); FontHelper.text(current.formattedList, 7, 16, 12, 5, 25, LogisticsColours.white_text.getRGB()); } */

	}

	public String getDisplayName() {
		return stack.getDisplayName();
	}

	public ArrayList<ElementInfo> getPageInfo(GuiGuide gui, ArrayList<ElementInfo> pageInfo) {
		pageInfo.add(new ElementInfo("guide." + unlocalizedName.substring(5) + ".name", new String[0]));
		return pageInfo;
	}

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

}
