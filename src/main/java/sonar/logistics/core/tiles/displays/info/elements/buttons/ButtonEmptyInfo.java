package sonar.logistics.core.tiles.displays.info.elements.buttons;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.client.gui.IGuiOrigin;
import sonar.core.handlers.inventories.containers.ContainerMultipartSync;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.ASMDisplayElement;
import sonar.logistics.api.core.tiles.displays.info.InfoUUID;
import sonar.logistics.base.requests.info.GuiInfoSource;
import sonar.logistics.base.requests.info.IInfoRequirement;
import sonar.logistics.core.tiles.displays.gsi.DisplayGSI;
import sonar.logistics.core.tiles.displays.gsi.interaction.DisplayScreenClick;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIElementPacketHelper;
import sonar.logistics.core.tiles.displays.info.InfoPacketHelper;
import sonar.logistics.core.tiles.displays.info.elements.UnconfiguredInfoElement;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.ArrayList;
import java.util.List;

@ASMDisplayElement(id = ButtonEmptyInfo.REGISTRY_NAME, modid = PL2Constants.MODID)
public class ButtonEmptyInfo extends ButtonElement implements IFlexibleGui<TileAbstractDisplay>, IInfoRequirement {

	public ButtonEmptyInfo() {
		super(0, 2, 15, "SELECT SOURCE");
	}

	//// GUI \\\\
	
	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		return 0;
	}

	@Override
	public void onGuiOpened(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag){
		InfoPacketHelper.sendLocalProvidersFromScreen(obj, world, obj.getPos(), player);
    }
	
	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new ContainerMultipartSync(obj);
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new GuiInfoSource(this, getGSI(), new ContainerMultipartSync(obj));
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return IGuiOrigin.withOrigin(new GuiInfoSource(this, getGSI(), new ContainerMultipartSync(obj)), origin);
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
		GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createInfoRequirementPacket(selected), getElementIdentity(), getGSI());
	}

	public void doInfoRequirementPacket(DisplayGSI gsi, EntityPlayer player, List<InfoUUID> require) {
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
