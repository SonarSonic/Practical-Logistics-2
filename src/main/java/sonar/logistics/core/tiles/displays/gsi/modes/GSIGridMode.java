package sonar.logistics.core.tiles.displays.gsi.modes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.utils.CustomColour;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.interaction.GSIInteractionHelper;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIElementPacketHelper;
import sonar.logistics.core.tiles.displays.gsi.storage.DisplayElementContainer;
import sonar.logistics.core.tiles.displays.info.elements.DisplayElementHelper;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

public class GSIGridMode implements IGSIMode {

	public DisplayGSI gsi;
	public double[] clickPosition1;
	public double[] clickPosition2;

	/// grid selection mode
	public GSICreateInfo createInfo;
	public int containerResizing;
	
	public GSIGridMode(DisplayGSI gsi) {
		this.gsi = gsi;
	}

	@Override
	public boolean onClicked(TileAbstractDisplay part, BlockPos pos, DisplayScreenClick click, BlockInteractionType type, EntityPlayer player) {
		if (type.isShifting()) {
			if (type.isLeft() || clickPosition1 == null) {
				exitGridSelectionMode();
			} else {
				finishGridSelectionMode();
			}
		} else {
			double[] newPosition = new double[] { click.clickX, click.clickY };
			if (clickPosition1 == null) {
				clickPosition1 = newPosition;

			} else if (clickPosition2 == null) {
				clickPosition2 = newPosition;
			} else if (type.isLeft()) {
				clickPosition1 = newPosition;
			} else {
				clickPosition2 = newPosition;
			}
		}
		return true;
	}

	public void renderMode() {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, -0.01);

		// render the other containers
		for (DisplayElementContainer container : gsi.containers.values()) {
			if (!gsi.isEditContainer(container) && containerResizing != container.getContainerIdentity()) {
				double[] translation = container.getTranslation();
				double[] scaling = container.getContainerMaxScaling();
				DisplayElementHelper.drawRect(translation[0], translation[1], translation[0] + scaling[0], translation[1] + scaling[1], new CustomColour(255, 153, 51).getRGB());
			}
		}

		/// renders the click selections
		if (clickPosition1 != null) {
			GlStateManager.translate(0, 0, 0.001);
			double[] click2 = clickPosition2 == null ? clickPosition1 : clickPosition2;
			double clickStartX = GSIInteractionHelper.getGridXPosition(gsi, Math.min(clickPosition1[0], click2[0]));
			double clickStartY = GSIInteractionHelper.getGridYPosition(gsi, Math.min(clickPosition1[1], click2[1]));
			double clickEndX = Math.min(gsi.getDisplayScaling()[0], GSIInteractionHelper.getGridXPosition(gsi, Math.max(clickPosition1[0], click2[0])) + GSIInteractionHelper.getGridXScale(gsi));
			double clickEndY = Math.min(gsi.getDisplayScaling()[1], GSIInteractionHelper.getGridYPosition(gsi, Math.max(clickPosition1[1], click2[1])) + GSIInteractionHelper.getGridYScale(gsi));
			DisplayElementHelper.drawRect(clickStartX, clickStartY, clickEndX, clickEndY, new CustomColour(49, 145, 88).getRGB());

			GlStateManager.translate(0, 0, -0.001);
		}

		/// render the grid
		GlStateManager.translate(0, 0, -0.001);
		CustomColour green = new CustomColour(174, 227, 227);
		DisplayElementHelper.drawGrid(0, 0, gsi.getDisplayScaling()[0], gsi.getDisplayScaling()[1], GSIInteractionHelper.getGridXScale(gsi), GSIInteractionHelper.getGridYScale(gsi), green.getRGB());

		/// render help overlays
		GlStateManager.popMatrix();
	}

	@Override
	public boolean renderElements() {
		return false;
	}

	@Override
	public boolean renderEditContainer() {
		return false;
	}

	public void startResizeSelectionMode(int containerID) {
		DisplayElementContainer c = gsi.getContainer(containerID);
		if (c != null) {
			createInfo = null;
			gsi.mode = gsi.grid_mode;
			containerResizing = c.getContainerIdentity();
			clickPosition1 = c.getTranslation();
			clickPosition2 = new double[] { c.getTranslation()[0] + c.getContainerMaxScaling()[0] - 0.0625, c.getTranslation()[1] + c.getContainerMaxScaling()[1] - 0.0625, 0 };

			Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("" + //
					TextFormatting.GREEN + "L-CLICK" + TextFormatting.RESET + " = FIRST POSITION, " + //
					TextFormatting.GREEN + "R-CLICK" + TextFormatting.RESET + " = SECOND POSITION, " + //
					TextFormatting.GREEN + "SHIFT-R" + TextFormatting.RESET + " = CONFIRM, " + //
					TextFormatting.RED + "SHIFT-L" + TextFormatting.RESET + " = CANCEL"));
		}
	}

	public void startGridSelectionMode(GSICreateInfo type) {
		createInfo = type;
		gsi.mode = gsi.grid_mode;
		clickPosition1 = null;
		clickPosition2 = null;

		Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation("" + //
				TextFormatting.GREEN + "L-CLICK" + TextFormatting.RESET + " = FIRST POSITION, " + //
				TextFormatting.GREEN + "R-CLICK" + TextFormatting.RESET + " = SECOND POSITION, " + //
				TextFormatting.GREEN + "SHIFT-R" + TextFormatting.RESET + " = CONFIRM, " + TextFormatting.RED + "SHIFT-L" + TextFormatting.RESET + " = CANCEL"));
	}

	public void exitGridSelectionMode() {
		gsi.mode = gsi.default_mode;
		clickPosition1 = null;
		clickPosition2 = null;
	}

	public void finishGridSelectionMode() {
		double[] click2 = clickPosition2 == null ? clickPosition1 : clickPosition2;
		double clickStartX = GSIInteractionHelper.getGridXPosition(gsi, Math.min(clickPosition1[0], click2[0]));
		double clickStartY = GSIInteractionHelper.getGridYPosition(gsi, Math.min(clickPosition1[1], click2[1]));
		double clickEndX = Math.min(gsi.getDisplayScaling()[0], GSIInteractionHelper.getGridXPosition(gsi, Math.max(clickPosition1[0], click2[0])) + GSIInteractionHelper.getGridXScale(gsi));
		double clickEndY = Math.min(gsi.getDisplayScaling()[1], GSIInteractionHelper.getGridYPosition(gsi, Math.max(clickPosition1[1], click2[1])) + GSIInteractionHelper.getGridYScale(gsi));
		if (createInfo != null) {
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createInfoAdditionPacket(new double[] { clickStartX, clickStartY, 0 }, new double[] { clickEndX - clickStartX, clickEndY - clickStartY, 1 }, 0.5, createInfo), -1, gsi);
		} else {
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createResizeContainerPacket(this.containerResizing, new double[] { clickStartX, clickStartY, 0 }, new double[] { clickEndX - clickStartX, clickEndY - clickStartY, 1 }, 0.5), -1, gsi);
			containerResizing = -1;
		}
		exitGridSelectionMode();
	}
}
