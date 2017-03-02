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
import sonar.logistics.info.LogicInfoRegistry.RegistryType;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.ProgressInfo;

public abstract class BaseItemPage extends BaseInfoPage implements IGuidePage {

	public String unlocalizedName;
	public ItemStack stack;
	public ArrayList<GuidePageLink> links = new ArrayList();
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
		RenderHelper.renderStoredItemStackOverlay(stack, 0, 8, yPos - 1, null, true);
		RenderHelper.restoreBlendState();
		FontHelper.text(stack.getDisplayName(), 28, yPos + 3, -1);
	}

	public void drawPage(GuiGuide gui, int x, int y, int page) {
		super.drawPage(gui, x, y, page);
		/* GuidePageInfoFormatted current = pageInfo.get("guide." + unlocalizedName.substring(5) + ".name"); if (current != null) { FontHelper.text(current.formattedList, 0, 7, 12, 96, 25, LogisticsColours.white_text.getRGB()); FontHelper.text(current.formattedList, 7, 16, 12, 5, 25, LogisticsColours.white_text.getRGB()); } */


	}

	public String getDisplayName() {
		return stack.getDisplayName();
	}

	public ArrayList<GuidePageInfo> getPageInfo(ArrayList<GuidePageInfo> pageInfo) {
		pageInfo.add(new GuidePageInfo("guide." + unlocalizedName.substring(5) + ".name", new String[0]));
		return pageInfo;
	}

	public ArrayList<IGuidePageElement> getElements(ArrayList<IGuidePageElement> elements) {
		super.getElements(elements);
		elements.add(new ElementGuideItem(0, stack, 1, 4));
		return elements;
	}

}
