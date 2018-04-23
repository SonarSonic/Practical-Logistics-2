package sonar.logistics.api.displays.elements;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.helpers.FontHelper;
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.displays.HeightAlignment;
import sonar.logistics.api.displays.WidthAlignment;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.lists.types.AbstractChangeableList;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

public interface IDisplayElement extends IDisplayRenderable, INBTSyncable {

	/**the id this {@link IDisplayElement} is registered to*/
	String getRegisteredName();	

	/**a string to represent what this {@link IDisplayElement} is in a GUI*/
	String getRepresentiveString();
	
	/**sets the {@link IElementStorageHolder} of this {@link IDisplayElement}*/
	IElementStorageHolder setHolder(IElementStorageHolder c);

	/**gets the {@link IElementStorageHolder} of this {@link IDisplayElement}*/
	IElementStorageHolder getHolder();

	/**the unique  identity of this element, used to identify the element between server and client*/
	int getElementIdentity();

	/**gets the {@link DisplayGSI} [Guided Screen Interface] which features this Display Element*/
	default DisplayGSI getGSI() {
		return getHolder().getContainer().getGSI();
	}

	//// IDisplayRenderable \\\\
	
	default void render() {
		FontHelper.text(getRepresentiveString(), 0, 0, getHolder().getContainer().getDefaultColour());
	}

	default void updateRender() {
		// CAN BE USED TO MARK THE ELEMENT AS CHANGED
	}

	void onElementChanged();

	double getPercentageFill();

	double setPercentageFill(double fill);


	/** the width/height in pixels, returns 0,0 if this element fills the entire container */
	default ElementFillType getFillType() {
		return ElementFillType.CUSTOM_SIZE;
	}

	int[] getUnscaledWidthHeight();

	public default double[] getAlignmentTranslation(double[] maxScaling, double[] actualScaling) {
		return DisplayElementHelper.alignArray(maxScaling, actualScaling, getWidthAlignment(), getHeightAlignment());
	}		
	
	WidthAlignment getWidthAlignment();

	WidthAlignment setWidthAlignment(WidthAlignment align);

	HeightAlignment getHeightAlignment();

	HeightAlignment setHeightAlignment(HeightAlignment align);
	
	
	double[] setMaxScaling(double[] scaling);

	double[] getMaxScaling();

	double[] setActualScaling(double[] scaling);

	double[] getActualScaling();

	default Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player){
		return null;
	}
	
	default void validate(DisplayGSI gsi){}
	
	default void invalidate(DisplayGSI gsi){}

}
