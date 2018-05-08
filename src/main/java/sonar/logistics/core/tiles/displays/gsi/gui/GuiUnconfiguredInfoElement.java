package sonar.logistics.core.tiles.displays.gsi.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.core.tiles.displays.gsi.packets.GSIElementPacketHelper;
import sonar.logistics.core.tiles.displays.info.elements.DisplayElementHelper;
import sonar.logistics.core.tiles.displays.info.elements.UnconfiguredInfoElement;
import sonar.logistics.core.tiles.displays.info.elements.base.IDisplayElement;
import sonar.logistics.core.tiles.displays.tiles.TileAbstractDisplay;

import java.util.ArrayList;
import java.util.List;

/** basically passes through the unconfigured element list, into different guis */
public class GuiUnconfiguredInfoElement extends GuiLogistics {
	public GuiScreen origin = null;
	public UnconfiguredInfoElement element;
	public TileAbstractDisplay display;
	public List<IDisplayElement> configured = new ArrayList<>();
	public int count = 0;

	public GuiUnconfiguredInfoElement(TileAbstractDisplay display, UnconfiguredInfoElement element, Object origin) {
		super(new ContainerMultipartSync(display), display);
		this.display = display;
		this.element = element;
		if (origin instanceof GuiScreen) {
			this.origin = (GuiScreen) origin;
		}
	}

	@Override
	public void initGui() {
		//calling getInfoElements() makes sure the elements are not null.
		while (count < element.getInfoElements().size()) {
			IDisplayElement e = element.elements.get(count);
			count++;
			if (e != null) {
				IDisplayElement copy = DisplayElementHelper.loadElement(DisplayElementHelper.saveElement(new NBTTagCompound(), e, SyncType.SAVE), element.getHolder());
				if (copy != null) {
					configured.add(copy);
					Object editScreen = copy.getClientEditGui(display, this, mc.world, mc.player);
					if (editScreen != null) {
						FMLClientHandler.instance().showGuiScreen(editScreen);
						return;
					}
				}
			}
		}
		if(!configured.isEmpty()) {
			element.elements = configured;
			GSIElementPacketHelper.sendGSIPacket(GSIElementPacketHelper.createConfigureInfoPacket(element), element.getElementIdentity(), element.getGSI());
		}
		FMLClientHandler.instance().showGuiScreen(origin);
	}

}
