package sonar.logistics.base.requests.colour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import sonar.logistics.base.gui.GuiLogistics;
import sonar.logistics.base.gui.PL2Colours;

import javax.annotation.Nonnull;

public class SavedColourButton extends GuiButton {

    public final GuiLogistics gui;
    public int last_colour_id;

    public SavedColourButton(GuiLogistics gui, int id, int last_colour_id, int x, int y, String hover) {
        super(id, x, y, 12, 12, hover);
        this.gui = gui;
    }

    public int getColour(){
        if(gui.getLastColours().size() > last_colour_id){
            return gui.getLastColours().get(last_colour_id);
        }
        return 0;
    }

    public boolean isSelected(){
        return gui.getCurrentColour() == getColour();
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int x, int y, float partialTicks) {
        if (this.visible) {
            this.hovered = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            boolean isSelected = isSelected();;
            drawRect(this.x, this.y, this.x + 12, this.y + 12, PL2Colours.blue_overlay.getRGB());
            drawRect(this.x + 1, this.y + 1, this.x + 11, this.y + 11, getColour());
        }

    }

    @Override
    public void drawButtonForegroundLayer(int x, int y) {
        if (hovered) {
            gui.drawSonarCreativeTabHoveringText(this.displayString, x, y);
        }
    }
}