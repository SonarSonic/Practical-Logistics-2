package sonar.logistics.client.gsi;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.render.DisplayInfo;
import sonar.logistics.api.info.render.IDisplayInfo;
import sonar.logistics.api.info.render.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;

/**GSI=GUIDED SCREEN INTERFACE*/
@SideOnly(Side.CLIENT)
public interface IGSI<I extends IInfo> {
	
	/**currently unimplemented, if you return true, other things may not work properly*/
	default boolean shouldCoverEntireDisplay(){
		return false;
	}
	
	default void onButtonClicked(GSIButton button, DisplayScreenClick click, EnumHand hand) {}
	
	void initGSI(DisplayInfo renderInfo);
	
	void resetGSI();
	
	void renderGSIBackground(I info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos);

	void renderGSIForeground(I info, InfoContainer container, IDisplayInfo displayInfo, double width, double height, double scale, int infoPos);
	
	void onGSIClicked(I info, DisplayScreenClick click, EnumHand hand);

	boolean canInteractWith(I info, DisplayScreenClick click, EnumHand hand);
}
