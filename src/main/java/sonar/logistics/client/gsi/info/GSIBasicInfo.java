package sonar.logistics.client.gsi.info;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.RenderHelper;
import sonar.core.helpers.SonarHelper;
import sonar.logistics.PL2Blocks;
import sonar.logistics.PL2Items;
import sonar.logistics.api.displays.IDisplayElement;
import sonar.logistics.api.displays.IDisplayInfo;
import sonar.logistics.api.displays.IDisplayRenderable;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.displays.elements.DisplayElementContainer;
import sonar.logistics.api.displays.elements.ItemStackElement;
import sonar.logistics.api.displays.elements.TextDisplayElement;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.render.RenderInfoProperties;
import sonar.logistics.api.tiles.displays.DisplayConstants;
import sonar.logistics.client.gsi.AbstractGSI;
import sonar.logistics.client.gsi.GSIButton;
import sonar.logistics.helpers.InfoRenderer;
import sonar.logistics.info.types.InfoError;

public class GSIBasicInfo extends AbstractGSI<IInfo> {

	public DisplayElementContainer list;

	@Override
	public void initRenderables(List<IDisplayRenderable> renderables) {
		super.initRenderables(renderables);
		RenderInfoProperties props = renderInfo.getRenderProperties();
		double[] scaling = props.getScaling();
		list = new DisplayElementContainer(scaling[0], scaling[1], scaling[2]);
		
		boolean first = true;
		for (String s : renderInfo.getFormattedStrings()) {
			IDisplayElement element = list.addElement(new TextDisplayElement(list, s));
			//element.setPercentageFill(first ? 0.75 : 0.1);
			first = false;
		}
		/*
		ArrayList<String> unpackedArray = Lists.newArrayList();
		unpackedArray.add(TextFormatting.BOLD + ""+ TextFormatting.UNDERLINE + "Videotape by Radiohead");
		List<String> strings = SonarHelper.convertArray(FontHelper.translate("When I'm at the pearly gates-This will be on my videotape, my videotape-Mephistopheles is just beneath-And he's reaching up to grab me- + -This is one for the good days-And I have it all here-In red, blue, green-Red, blue, green- + -You are my center-When I spin away-Out of control on videotape-On videotape-On videotape-On videotape-On videotape-On videotape- + -This is my way of saying goodbye-Because I can't do it face to face-I'm talking to you after it's too late-No matter what happens now-You shouldn't be afraid-Because I know today has been-the most perfect day I've ever seen").split("-"));
		for(String s : strings){
			FontHelper.breakLines(unpackedArray, s, (int)(scaling[0]/0.0625)*16);
		}
		for(String t : unpackedArray){
			list.addElement(new TextDisplayElement(list, t));
		}
		*/
		renderables.add(list);
		/*
		DisplayElementList item = new DisplayElementList(scaling[0], scaling[1], scaling[2]);
		item.addElement(new ItemStackElement(item, new StoredItemStack(new ItemStack(PL2Blocks.large_display_screen,5))));
		renderables.add(item);
		*/
	}

	@Override
	public void renderGSIForeground(IInfo info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos) {
		super.renderGSIForeground(info, container, displayInfo, width, height, scale, infoPos);
		/*
		ArrayList<String> unpackedArray = Lists.newArrayList();
		unpackedArray.add(TextFormatting.BOLD + ""+ TextFormatting.UNDERLINE + "Videotape by Radiohead");
		List<String> strings = SonarHelper.convertArray(FontHelper.translate("When I'm at the pearly gates-This will be on my videotape, my videotape-Mephistopheles is just beneath-And he's reaching up to grab me- + -This is one for the good days-And I have it all here-In red, blue, green-Red, blue, green- + -You are my center-When I spin away-Out of control on videotape-On videotape-On videotape-On videotape-On videotape-On videotape- + -This is my way of saying goodbye-Because I can't do it face to face-I'm talking to you after it's too late-No matter what happens now-You shouldn't be afraid-Because I know today has been-the most perfect day I've ever seen").split("-"));
		for(String s : strings){
			FontHelper.breakLines(unpackedArray, s, (int)(width/0.0625)*16);
		}
		
		InfoRenderer.renderCenteredStringsWithUniformScaling(width, height, 0, 12, 1, -1, unpackedArray);
		*/
	}

}
