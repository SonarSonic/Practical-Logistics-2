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
import sonar.logistics.client.LogisticsColours;
import sonar.logistics.client.gui.display.GuiEditProgressBar;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.helpers.DisplayElementHelper;
import sonar.logistics.info.types.ProgressInfo;

@DisplayElementType(id = ProgressBarElement.REGISTRY_NAME, modid = PL2Constants.MODID)
public class ProgressBarElement extends AbstractInfoElement<ProgressInfo> {

	public int colour = -11141291;
	public ProgressBarDirection direction = ProgressBarDirection.RIGHT;
	public ProgressBarType barType = ProgressBarType.FILL;
	public double border_thickness = 0;
	public int border_colour = LogisticsColours.white_text.getRGB();
	public int background_colour = 0;

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
		double bar_start_width = 0;
		double bar_start_height = 0;
		double bar_end_width = getActualScaling()[0];
		double bar_end_height = getActualScaling()[1];

		double actual_border_thickness = Math.min(Math.min((bar_end_width / 4) - 0.00625, (bar_end_height / 4) - 0.00625), border_thickness);

		GlStateManager.translate(0, 0, 0.002);
		if (actual_border_thickness != 0) {
			DisplayElementHelper.drawRect(0, 0, getActualScaling()[0], actual_border_thickness, border_colour);
			DisplayElementHelper.drawRect(0, getActualScaling()[1] - actual_border_thickness, getActualScaling()[0], getActualScaling()[1], border_colour);
			DisplayElementHelper.drawRect(0, 0, actual_border_thickness, getActualScaling()[1], border_colour);
			DisplayElementHelper.drawRect(getActualScaling()[0], 0, getActualScaling()[0] - actual_border_thickness, getActualScaling()[1], border_colour);
		}
		GlStateManager.translate(0, 0, -0.001);
		if (background_colour != 0) {
			DisplayElementHelper.drawRect(bar_start_width + actual_border_thickness, bar_start_height + actual_border_thickness, bar_end_width - actual_border_thickness, bar_end_height - border_thickness, background_colour);
		}
		bar_start_width += actual_border_thickness * 2;
		bar_start_height += actual_border_thickness * 2;
		bar_end_width -= actual_border_thickness * 2;
		bar_end_height -= actual_border_thickness * 2;
		// DisplayElementHelper.drawRect(border_thickness, border_thickness, getActualScaling()[0] - border_thickness, getActualScaling()[1] - border_thickness, LogisticsColours.grey_base.getRGB());
		GlStateManager.translate(0, 0, -0.001);

		double actual_width = bar_end_width - bar_start_width;
		double actual_height = bar_end_height - bar_start_height;

		switch (barType) {
		case FILL:
			switch (direction) {
			case LEFT:
				double barWidth = num1 * (actual_width) / num2;
				DisplayElementHelper.drawRect(bar_end_width - barWidth, bar_start_height, bar_end_width, bar_end_height, colour);
				break;
			case RIGHT:
				barWidth = num1 * (actual_width) / num2;
				DisplayElementHelper.drawRect(bar_start_width, bar_start_height, bar_start_width + barWidth, bar_end_height, colour);
				break;
			case UP:
				double barHeight = num1 * (actual_height) / num2;
				DisplayElementHelper.drawRect(bar_start_width, bar_end_height - barHeight, bar_end_width, bar_end_height, colour);
				break;
			case DOWN:
				barHeight = num1 * (actual_height) / num2;
				DisplayElementHelper.drawRect(bar_start_width, bar_start_height, bar_end_width, bar_start_height + barHeight, colour);
				break;
			default:
				break;
			}
			break;
		case BARS:
			int count = (int) (num1 * 20 / num2);
			double w = (actual_width / 40);
			double h = (actual_height / 40);
			switch (direction) {
			case LEFT:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(bar_end_width - (w + (w * (i * 2) - w / 2)), bar_start_height, bar_end_width - (w + (w * (i * 2) - w / 2) + w), bar_end_height, colour);
				}
				break;
			case RIGHT:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(bar_start_width + (w + (w * (i * 2) - w / 2)), bar_start_height, bar_start_width + (w + (w * (i * 2) - w / 2) + w), bar_end_height, colour);
				}
				break;
			case UP:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(bar_start_width, bar_end_height - (h + (h * (i * 2) - h / 2)), bar_end_width, bar_end_height - (h + (h * (i * 2) - h / 2) + h), colour);
				}
				break;
			case DOWN:
				for (int i = 0; i <= count; i++) {
					DisplayElementHelper.drawRect(bar_start_width, bar_start_height + h + (h * (i * 2) - h / 2), bar_end_width, bar_start_height + h + (h * (i * 2) - h / 2) + h, colour);
				}
				break;
			default:
				break;
			}
			break;
		case SCROLLER:
			w = actual_width / 20;
			h = actual_height / 20;
			double wPos = (num1 * ((actual_width) - w) / num2);
			double hPos = (num1 * ((actual_height) - h) / num2);
			switch (direction) {
			case LEFT:
				DisplayElementHelper.drawRect(bar_end_width - wPos, bar_start_height, bar_end_width - wPos - w, bar_end_height, colour);
				break;
			case RIGHT:
				DisplayElementHelper.drawRect(bar_start_width + wPos, bar_start_height, bar_start_width + wPos + w, bar_end_height, colour);
				break;
			case UP:
				DisplayElementHelper.drawRect(bar_start_width, bar_end_height - hPos, bar_end_width, bar_end_height - hPos - h, colour);
				break;
			case DOWN:
				DisplayElementHelper.drawRect(bar_start_width, bar_start_height + hPos, bar_end_width, bar_start_height + hPos + h, colour);
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
		border_thickness = nbt.getDouble("b_thickness");
		border_colour = nbt.getInteger("b_colour");
		background_colour = nbt.getInteger("bgd_colour");
	}

	@Override
	public NBTTagCompound writeData(NBTTagCompound nbt, SyncType type) {
		super.writeData(nbt, type);
		nbt.setInteger("c", colour);
		nbt.setByte("dir", (byte) direction.ordinal());
		nbt.setByte("barType", (byte) barType.ordinal());
		nbt.setDouble("b_thickness", border_thickness);
		nbt.setInteger("b_colour", border_colour);
		nbt.setInteger("bgd_colour", background_colour);
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
