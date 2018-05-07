package sonar.logistics.api.displays.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import sonar.logistics.api.displays.CreateInfoType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.displays.buttons.ButtonCreateElement;
import sonar.logistics.api.displays.buttons.ButtonElement;
import sonar.logistics.api.displays.tiles.DisplayScreenClick;
import sonar.logistics.api.displays.tiles.DisplayType;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.holographic.TileAbstractHolographicDisplay;

import static net.minecraft.client.renderer.GlStateManager.translate;

public class EditContainer extends DisplayElementContainer {
	
	public static EditContainer addEditContainer(DisplayGSI gsi) {
		double[] scaling = new double[] { gsi.display.getDisplayType().width / 4, Math.min(gsi.getDisplayScaling()[1], gsi.display.getDisplayType().height), 1 };
		if(gsi.display.getDisplayType()==DisplayType.CONNECTED || gsi.display.getDisplayType()==DisplayType.LARGE){
			scaling[1]=gsi.display.getDisplayType().height/1.5;
		}


		EditContainer editContainer = new EditContainer(gsi, new double[] { 0, 0, 0 }, scaling, 1, DisplayGSI.EDIT_CONTAINER_ID);
		gsi.containers.put(DisplayGSI.EDIT_CONTAINER_ID, editContainer);
		editContainer.lock();
		DisplayElementList editList = new DisplayElementList();

		editList.setWidthAlignment(WidthAlignment.LEFT);
		editList.setHeightAlignment(HeightAlignment.TOP);
		editContainer.getElements().addElement(editList);
		editList.getElements().addElement(new ButtonCreateElement(CreateInfoType.INFO, 0, 11, 10, "CREATE INFO"));
		editList.getElements().addElement(new ButtonCreateElement(CreateInfoType.TITLE, 1, 11, 11, "CREATE TITLE"));
		editList.getElements().addElement(new ButtonCreateElement(CreateInfoType.WRAPPED_TEXT, 2, 11, 14, "CREATE WRAPPED TEXT"));
		///editList.getElements().addElement(new ButtonElementSelection(ElementSelectionType.DELETE, 2, 2, 2, "DELETE ELEMENTS"));
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
		if(gsi.getDisplay() instanceof TileAbstractHolographicDisplay) {
			editList.getElements().addElement(new ButtonElement(5, 12, 1, "EDIT HOLOGRAPHIC DISPLAY SCALING") {

				@Override
				public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
					click.gsi.requestGui((TileAbstractDisplay) click.gsi.display.getActualDisplay(), player.world, player.getPosition(), player, -1, 2, new NBTTagCompound());
					return -1;

				}
			});
		}
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
		translate(0, 0, -0.002);
		super.render();
		translate(0, 0, 0.002);
	}
	public boolean canRender() {
		return isWithinScreenBounds && gsi.edit_mode.getObject() && !gsi.isGridSelectionMode;
	}
}
