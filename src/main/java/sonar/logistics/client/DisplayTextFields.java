package sonar.logistics.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;

public class DisplayTextFields extends Gui {

	public GuiTextField[] fields;
	public int x, y, size;
	public int width = 160, height = 12;

	public DisplayTextFields(int x, int y, int size) {
		this.x = x;
		this.y = y;
		this.size = size;
	}

	public void initFields(List<String> textList) {
		Keyboard.enableRepeatEvents(true);
		this.fields = new GuiTextField[size];
		FontRenderer render = Minecraft.getMinecraft().fontRenderer;
		for (int i = 0; i < fields.length; i++) {
			GuiTextField field = new GuiTextField(i, render, x, y + i * height, width, height);
			field.setMaxStringLength(64);
			field.setText(textList.size() > i ? textList.get(i) : "");
			field.setEnableBackgroundDrawing(false);
			fields[i] = field;
		}
	}

	public GuiTextField getSelectedField() {
        for (GuiTextField field : fields) {
            if (field.isFocused()) {
                return field;
            }
        }
		return null;
	}

	public int getSelectedPos() {
		for (int i = 0; i < fields.length; i++) {
			GuiTextField field = fields[i];
			if (field.isFocused()) {
				return i;
			}
		}
		return -1;
	}

	public void drawTextBox() {
		drawRect(x - 1, y - 1, x + this.width + 1, y + this.height * size + 1, -6250336);
		drawRect(x, y, x + this.width, y + this.height * size, -16777216);
        for (GuiTextField field : fields) {
            field.drawTextBox();
        }
	}

	public boolean isFocused() {
		return this.getSelectedField() != null;
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean flag = mouseX >= x && mouseX < x + this.width && mouseY >= y && mouseY < y + height * size;
		if (!flag) {
			return;
		}
        for (GuiTextField field : fields) {
            field.mouseClicked(mouseX, mouseY, mouseButton);
        }
	}

	public void keyTyped(char typedChar, int keyCode) {
		for (int i = 0; i < fields.length; i++) {
			GuiTextField field = fields[i];
			if (field.isFocused()) {
				if (typedChar == 27) {
					field.setFocused(false);
				} else {
					if (keyCode == Keyboard.KEY_UP) {
						field.setFocused(false);
						if (i - 1 > 0) {
							fields[i - 1].setFocused(true);
						} else {
							fields[fields.length - 1].setFocused(true);
						}
					} else if (keyCode == Keyboard.KEY_DOWN || typedChar == 13) {
						field.setFocused(false);
						if (i + 1 < fields.length) {
							fields[i + 1].setFocused(true);
						} else {
							fields[0].setFocused(true);
						}
					} else {
						field.textboxKeyTyped(typedChar, keyCode);
					}
				}
				return;
			}
		}
	}

	public ArrayList<String> textList() {
		ArrayList<String> list = new ArrayList<>();
        for (GuiTextField field : fields) {
            list.add(field.getText());
        }
		return list;
	}
}
