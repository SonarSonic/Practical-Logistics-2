package sonar.logistics.api.filters;

import java.awt.Color;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import sonar.core.api.fluids.StoredFluidStack;
import sonar.core.client.gui.GuiSonar;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncNBTAbstractList;
import sonar.logistics.Logistics;
import sonar.logistics.api.asm.NodeFilter;
import sonar.logistics.api.nodes.TransferType;

@NodeFilter(id = FluidFilter.id, modid = Logistics.MODID)
public class FluidFilter extends BaseFilter implements IFluidFilter {

	public static final String id = "fluid";

	public SyncNBTAbstractList<StoredFluidStack> list = new SyncNBTAbstractList<StoredFluidStack>(StoredFluidStack.class, 1);
	{
		syncList.addPart(list);
	}

	@Override
	public String getNodeID() {
		return id;
	}

	public ArrayList<StoredFluidStack> getFluidFilters() {
		return list.objs;
	}

	public void addFluid(StoredFluidStack stack) {
		for (StoredFluidStack fluid : list.objs) {
			if (fluid.equalStack(stack.fluid)) {
				return;
			}
		}
		list.addObject(stack);
	}

	public void removeFluid(StoredFluidStack stack) {
		ArrayList<StoredFluidStack> toRemove = new ArrayList();
		for (StoredFluidStack fluid : list.objs) {
			if (fluid.equalStack(stack.fluid)) {
				toRemove.add(fluid);
			}
		}
		toRemove.forEach(remove -> list.removeObject(remove));
	}

	@Override
	public boolean canTransferFluid(StoredFluidStack stack) {
		for (StoredFluidStack fluid : list.objs) {
			if (fluid.equalStack(stack.fluid)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TransferType[] getTypes() {
		return new TransferType[] { TransferType.FLUID };
	}

	@Override
	public void renderInfoInList(GuiSonar screen, int yPos) {
		GlStateManager.scale(0.75, 0.75, 0.75);
		FontHelper.text("Fluid Filter", 16, (int)((yPos + 2)*1/0.75), Color.white.getRGB());		
		FontHelper.text("Type: " + this.getTransferMode().name(), 88, (int)((yPos + 2)*1/0.75), Color.white.getRGB());
		FontHelper.text("List Type: " + this.getListType().name(), 200, (int)((yPos + 2)*1/0.75), Color.white.getRGB());
		GlStateManager.scale(1/0.75, 1/0.75, 1/0.75);
		
		net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
		// GlStateManager.scale(1.0/0.75, 1.0/0.75, 1.0/0.75);
		GlStateManager.translate(0, 12, 0);
		int yOffset = 0;
		for (int i = 0; i < Math.min(12, list.objs.size()); i++) {
			if (i == 12) {
				yOffset++;
			}
			StoredFluidStack item = list.objs.get(i);

			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(item.fluid.getFluid().getStill().toString());
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			screen.drawTexturedModalRect(13 + (i * 18), -2 + yPos + (yOffset * 18), sprite, 16, 16);
			
		}
		GlStateManager.translate(0, -12, 0);
		// GlStateManager.scale(0.75, 0.75, 0.75);

	}

	@Override
	public boolean isValidFilter() {
		return !list.getObjects().isEmpty();
	}

}