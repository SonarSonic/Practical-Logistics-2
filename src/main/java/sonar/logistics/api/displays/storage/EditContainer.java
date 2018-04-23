package sonar.logistics.api.displays.storage;

import static net.minecraft.client.renderer.GlStateManager.translate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import sonar.logistics.api.displays.CreateInfoType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.buttons.ButtonElement;
import sonar.logistics.api.displays.buttons.CreateElementButton;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

public class EditContainer extends DisplayElementContainer {
	
	public static EditContainer addEditContainer(DisplayGSI gsi) {
		double[] scaling = new double[] { gsi.display.getDisplayType().width / 4, gsi.display.getDisplayType().height / 1.5, 1 };
		EditContainer editContainer = new EditContainer(gsi, new double[] { 0, 0, 0 }, scaling, 1, gsi.EDIT_CONTAINER_ID);
		gsi.containers.put(gsi.EDIT_CONTAINER_ID, editContainer);
		editContainer.lock();
		DisplayElementList editList = new DisplayElementList();

		editList.setWidthAlignment(WidthAlignment.LEFT);
		editList.setHeightAlignment(HeightAlignment.TOP);
		editContainer.getElements().addElement(editList);
		editList.getElements().addElement(new CreateElementButton(CreateInfoType.INFO, 0, 11, 10, "CREATE INFO"));
		editList.getElements().addElement(new CreateElementButton(CreateInfoType.TITLE, 1, 11, 11, "CREATE TITLE"));
		editList.getElements().addElement(new CreateElementButton(CreateInfoType.WRAPPED_TEXT, 2, 11, 14, "CREATE WRAPPED TEXT"));
		///editList.getElements().addElement(new ElementSelectionButton(ElementSelectionType.DELETE, 2, 2, 2, "DELETE ELEMENTS"));
		editList.getElements().addElement(new ButtonElement(3, 12, 0, "EDIT ELEMENTS"){

			@Override			
			public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
				click.gsi.requestGui((TileAbstractDisplay) click.gsi.display.getActualDisplay(), player.world, player.getPosition(), player, -1, 0, new NBTTagCompound());
				return -1;
				
			}
		});
		editList.getElements().addElement(new ButtonElement(4, 2, 11, "CLOSE EDIT MODE"){

			@Override			
			public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
				player.sendMessage(new TextComponentTranslation("SHIFT-R click the display to reopen edit mode"));
				GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createEditModePacket(false), -1, gsi);
				return -1;
				
			}
		});
	
		return editContainer;
	}

	public EditContainer() {}

	public EditContainer(DisplayGSI gsi, double xPos, double yPos, double zPos, double width, double height, double pScale, int identity) {
		super(gsi, xPos, yPos, zPos, width, height, pScale, identity);
	}

	public EditContainer(DisplayGSI gsi, double[] translate, double[] scale, double pScale, int identity) {
		super(gsi, translate, scale, pScale, identity);
	}

	public void render() {
		translate(0, 0, -0.01);
		super.render();
		translate(0, 0, 0.01);
	}
	public boolean canRender() {
		return isWithinScreenBounds && gsi.edit_mode.getObject() && !gsi.isGridSelectionMode;
	}
}
