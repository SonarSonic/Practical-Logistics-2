package sonar.logistics.api.displays.elements;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.helpers.RenderHelper;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.DisplayInfo;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.helpers.InfoRenderer;

@DisplayElementType(id = TextDisplayElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class TextDisplayElement extends AbstractDisplayElement {

	protected String unformattedText;

	public TextDisplayElement() {}
	
	public TextDisplayElement(DisplayElementContainer list, String unformattedText) {
		super(list);
		this.unformattedText = unformattedText;
	}

	@Override
	int[] createUnscaledWidthHeight() {
		return new int[]{InfoRenderer.getStringWidth(unformattedText), InfoRenderer.getStringHeight()};
	}
	
	@Override
	public String getRepresentiveString() {
		return unformattedText;
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		return Lists.newArrayList(); //FIXME
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		unformattedText = nbt.getString("text");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		nbt.setString("text", unformattedText);
		return nbt;
	}

	public static final String REGISTRY_NAME = "text_element";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}
}
