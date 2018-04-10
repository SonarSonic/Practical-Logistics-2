package sonar.logistics.api.displays.buttons;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.elements.IInfoRequirement;
import sonar.logistics.api.displays.elements.types.UnconfiguredInfoElement;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gsi.GSIElementPacketHelper;
import sonar.logistics.client.gui.GuiInfoSource;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.PacketHelper;

@DisplayElementType(id = EmptyInfoElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class EmptyInfoElement extends ButtonElement implements IFlexibleGui<TileAbstractDisplay>, IInfoRequirement {

	public EmptyInfoElement() {
		super(0, 2, 15, "SELECT SOURCE");
	}

	//// GUI \\\\
	
	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		return 0;
	}

	@Override
	public void onGuiOpened(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag){
		PacketHelper.sendLocalProvidersFromScreen(obj, world, obj.getPos(), player);
    }
	
	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new ContainerMultipartSync(obj);
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new GuiInfoSource(this, getGSI(), new ContainerMultipartSync(obj));
	}
	
	//// REQUIRED INFO \\\\
	
	@Override
	public int getRequired() {
		return 1;
	}

	@Override
	public List<InfoUUID> getSelectedInfo() {
		return new ArrayList<>(); // nothing selected at the moment
	}

	@Override
	public void onGuiClosed(List<InfoUUID> selected) {
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createInfoRequirementPacket(selected, 0), getElementIdentity(), getGSI());
	}
	
	//// SELECTED INFO \\\\

	public void doInfoRequirementPacket(DisplayGSI gsi, EntityPlayer player, List<InfoUUID> require, int requirementRef) {
		InfoUUID infoUUID = require.get(0);
		if(InfoUUID.valid(infoUUID)){	
			getHolder().getElements().addElement(new UnconfiguredInfoElement(infoUUID));	
			getHolder().getElements().removeElement(this);			
		}
	}

	public static final String REGISTRY_NAME = "empty_info";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
