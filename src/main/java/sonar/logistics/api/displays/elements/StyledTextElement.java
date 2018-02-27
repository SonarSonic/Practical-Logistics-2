package sonar.logistics.api.displays.elements;

import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import sonar.core.api.IFlexibleGui;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.inventory.ContainerMultipartSync;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.api.tiles.displays.DisplayScreenClick;
import sonar.logistics.client.gui.GuiEditTextElement;
import sonar.logistics.client.gui.textedit.GuiEditStyledStrings;
import sonar.logistics.client.gui.textedit.StyledString;
import sonar.logistics.client.gui.textedit.StyledStringLine;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;

@DisplayElementType(id = StyledTextElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class StyledTextElement extends AbstractDisplayElement implements IClickableElement, IFlexibleGui<TileAbstractDisplay>, Iterable<StyledStringLine> {

	public List<StyledStringLine> textLines = Lists.newArrayList();
	public int spacing = 0;

	public StyledTextElement() {}

	public StyledTextElement(String string) {
		this(Lists.newArrayList(string));
	}

	public StyledTextElement(List<String> strings) {
		strings.forEach(s -> textLines.add(new StyledStringLine(s)));
	}

	@Override
	public void render() {
		textLines.forEach(s -> {
			preRender(s);
			s.render();
			postRender(s);
		});
	}

	public void preRender(StyledStringLine c) {

	}

	public void postRender(StyledStringLine c) {
		GL11.glTranslated(0, 9, 0);
	}

	@Override
	public List<InfoUUID> getInfoReferences() {
		return Lists.newArrayList(); // FIXME
	}

	@Override
	public String getRepresentiveString() {
		return "TEXT LIST";
	}

	@Override
	public int onGSIClicked(DisplayScreenClick click, EntityPlayer player, double subClickX, double subClickY) {
		return 0;
	}

	public Tuple<StyledStringLine, Integer> getStringClicked(double subClickX, double subClickY) {
		double y = 0;
		int i = 0;
		for (StyledStringLine c : textLines) {
			double start = y;
			double end = y + (c.getStringWidth() * getActualScaling()[SCALE]);
			if (start <= subClickY && end >= subClickY) {
				// FIXME alignment translations
				return new Tuple(c, i);
			}
			y = end + (spacing * getActualScaling()[SCALE]);
					
			i++;
		}
		return new Tuple(null, -1);
	}

	@Override
	public Iterator<StyledStringLine> iterator() {
		return textLines.iterator();
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		List<StyledStringLine> newLines = Lists.newArrayList();
		NBTTagList tagList = nbt.getTagList("ssc", NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound ssTag = tagList.getCompoundTagAt(i);
			StyledStringLine ss = new StyledStringLine();
			ss.readData(ssTag, type);
			newLines.add(ss);
		}
		textLines = newLines;
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		NBTTagList tagList = new NBTTagList();
		for (StyledStringLine ss : textLines) {
			tagList.appendTag(ss.writeData(new NBTTagCompound(), type));
		}
		if (!tagList.hasNoTags()) {
			nbt.setTag("ssc", tagList);
		}
		return nbt;
	}

	@Override
	public Object getServerElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new ContainerMultipartSync(obj);
	}

	@Override
	public Object getClientElement(TileAbstractDisplay obj, int id, World world, EntityPlayer player, NBTTagCompound tag) {
		return new GuiEditStyledStrings(this, this.getHolder().getContainer());
	}

	public int getWidth() {
		int width = 0;
		for (StyledStringLine ss : this) {
			int w = ss.getStringWidth();
			if (w > width) {
				width = w;
			}
		}
		return width;
	}

	public int getHeight() {
		int height = 0;
		for (StyledStringLine ss : this) {
			int h = ss.getStringHeight() + spacing;
			if (h > height) {
				height = h;
			}
		}
		return height;
	}

	public int[] createUnscaledWidthHeight() {
		return new int[] { getWidth(), getHeight() };
	}

	public static final String REGISTRY_NAME = "styled_text";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
