package sonar.logistics.api.displays.elements.types;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.logistics.PL2Constants;
import sonar.logistics.api.asm.DisplayElementType;
import sonar.logistics.api.displays.elements.AbstractInfoElement;
import sonar.logistics.api.displays.elements.ElementFillType;
import sonar.logistics.api.info.IInfo;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.client.gui.display.GuiEditProgressBar;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.info.types.ProgressInfo;

@DisplayElementType(id = ProgressBarElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class ProgressBarElement extends AbstractInfoElement<ProgressInfo> {

	public int colour = -11141291;
	public ProgressBarDirection direction = ProgressBarDirection.RIGHT;
	public ProgressBarType barType = ProgressBarType.FILL;
	public double border_thickness;

	public ProgressBarElement() {
		super();
	}

	public ProgressBarElement(InfoUUID uuid) {
		super(uuid);
	}

	@Override
	public void render(ProgressInfo info) {
		GlStateManager.translate(0, 0, 0.002);
		double num1 = (info.compare == 1 ? info.secondNum : info.firstNum);
		double num2 = (info.compare == 1 ? info.firstNum : info.secondNum);
		switch (barType) {
		case FILL:
			switch (direction) {
			case LEFT:
				double barWidth = num1 * (getActualScaling()[0]) / num2;
				DisplayElementHelper.drawRect(getActualScaling()[0] - barWidth, 0, getActualScaling()[0], getActualScaling()[1], colour);
				break;
			case RIGHT:
				barWidth = num1 * (getActualScaling()[0]) / num2;
				DisplayElementHelper.drawRect(0, 0, barWidth, getActualScaling()[1], colour);
				break;
			case UP:
				double barHeight = num1 * (getActualScaling()[1]) / num2;
				DisplayElementHelper.drawRect(0, getActualScaling()[1] - barHeight, getActualScaling()[0], getActualScaling()[1], colour);
				break;
			case DOWN:
				barHeight = num1 * (getActualScaling()[1]) / num2;
				DisplayElementHelper.drawRect(0, 0, getActualScaling()[0], barHeight, colour);
				break;
			default:
				break;
			}
			break;
		case BARS:
			int count = (int) (num1 * 20 / num2);
			double w = getActualScaling()[0] / 40;
			double h = getActualScaling()[1] / 40;
			switch (direction) {
			case LEFT:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(getActualScaling()[0] - (w + (w * (i * 2) - w / 2)), 0, getActualScaling()[0] - (w + (w * (i * 2) - w / 2) + w), getActualScaling()[1], colour);
				}
				break;
			case RIGHT:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(w + (w * (i * 2) - w / 2), 0, w + (w * (i * 2) - w / 2) + w, getActualScaling()[1], colour);
				}
				break;
			case UP:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(0, getActualScaling()[1] - (h + (h * (i * 2) - h / 2)), getActualScaling()[0], getActualScaling()[1] - (h + (h * (i * 2) - h / 2) + h), colour);
				}
				break;
			case DOWN:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(0, h + (h * (i * 2) - h / 2), getActualScaling()[0], h + (h * (i * 2) - h / 2) + h, colour);
				}
				break;
			default:
				break;
			}
			break;
		case SCROLLER:
			w = getActualScaling()[0] / 20;
			h = getActualScaling()[1] / 20;
			double wPos = num1 * ((getActualScaling()[0])-w) / num2;
			switch (direction) {
			case LEFT:
				break;
			case RIGHT:
				DisplayElementHelper.drawRect(wPos, 0, wPos + w, getActualScaling()[1], colour);
				break;
			case UP:
				break;
			case DOWN:
				break;
			default:
				break;
			}
			break;
		default:
			break;

		}

		GlStateManager.translate(0, 0, -0.002);
	}

	@Override
	public Object getClientEditGui(TileAbstractDisplay obj, Object origin, World world, EntityPlayer player) {
		return GuiSonar.withOrigin(new GuiEditProgressBar(this, obj), origin);
	}

	@Override
	public boolean isType(IInfo info) {
		return info instanceof ProgressInfo;
	}

	@Override
	public ElementFillType getFillType() {
		return ElementFillType.FILL_CONTAINER;
	}

	@Override
	public String getRepresentiveString() {
		return "Progress Bar";
	}

	@Override
	public void readData(NBTTagCompound nbt, SyncType type) {
		super.readData(nbt, type);
		colour = nbt.getInteger("c");
		direction = ProgressBarDirection.values()[nbt.getByte("dir")];
		barType = ProgressBarType.values()[nbt.getByte("barType")];
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setInteger("c", colour);
		nbt.setByte("dir", (byte) direction.ordinal());
		nbt.setByte("barType", (byte) barType.ordinal());
		return nbt;
	}

	public static enum ProgressBarType {
		FILL, BARS, SCROLLER;
	}

	public static enum ProgressBarDirection {
		RIGHT, LEFT, UP, DOWN;
	}

	public static final String REGISTRY_NAME = "p_bar";

	@Override
	public String getRegisteredName() {
		return REGISTRY_NAME;
	}

}
