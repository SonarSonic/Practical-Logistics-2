package sonar.logistics.api.displays.elements;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.FontHelper;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.core.helpers.SonarTextFormatter;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.ITextElement;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gui.GuiEditTextElement;
import sonar.logistics.client.gui.textedit.StyledString;
import sonar.logistics.client.gui.textedit.StyledStringLine;
import sonar.logistics.client.gui.textedit.TextSelectionType;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.helpers.InfoRenderer;

@Deprecated
//@DisplayElementType(id = TextDisplayElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class TextDisplayElement extends AbstractDisplayElement implements ITextElement, IClickableElement, IFlexibleGui<TileAbstractDisplay> {

	protected StyledStringLine styledText;

	public TextDisplayElement() {}

	public TextDisplayElement(String unformattedText) {
		super();
		this.styledText = new StyledStringLine(unformattedText);
	}

	public void render() {
		styledText.render();
	}

	@Override
	int[] createUnscaledWidthHeight() {
		return new int[] { styledText.getStringWidth(), InfoRenderer.getStringHeight() };
	}

	@Override
	public String getRepresentiveString() {
		return styledText.getCachedFormattedString();
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		return Lists.newArrayList(); // FIXME
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		(styledText = new StyledStringLine()).readData(nbt, type);
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		styledText.writeData(nbt, type);
		return nbt;
	}

	public static final String REGISTRY_NAME = "text_element";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

	@Override
	public StyledStringLine getStyledStringCompound() {
		return styledText;
	}

	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new ContainerMultipartSync(obj);
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new GuiEditTextElement(this.getHolder().getContainer());
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		return 0;
	}
}
