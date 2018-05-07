package sonar.logistics.api.displays.actions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayActionType;
import sonar.logistics.api.displays.IDisplayAction;
import sonar.logistics.api.displays.tiles.DisplayScreenClick;

import java.net.URI;

@DisplayActionType(id = ClickHyperlink.REGISTRY_NAME, modid = PL2Constants.MODID)
public class ClickHyperlink implements IDisplayAction {
	
	public String hyperlink = "";
	
	public ClickHyperlink(){}
	
	public ClickHyperlink(String hyperlink){
		this.hyperlink = hyperlink;
	}
	
	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		hyperlink = nbt.getString("hyperlink");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setString("hyperlink", hyperlink);
		return nbt;
	}

	@Override
	public int doAction(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
        try
        {	URI uri = new URI(hyperlink);
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke(null);
            oclass.getMethod("browse", URI.class).invoke(object, uri);

			player.sendMessage(new TextComponentTranslation("Opened: " + hyperlink));
        }
        catch (Throwable throwable1)
        {
            Throwable throwable = throwable1.getCause();
            PL2.logger.error("Couldn't open link: {}", throwable == null ? "<UNKNOWN>" : throwable.getMessage());
            
        }
		return -1;
	}

	public static final String REGISTRY_NAME = "hyperlink_action";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
