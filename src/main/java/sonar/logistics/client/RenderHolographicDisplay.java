package sonar.logistics.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import sonar.logistics.common.multiparts.holographic.EntityHolographicDisplay;
import sonar.logistics.common.multiparts.holographic.TileAbstractHolographicDisplay;
import sonar.logistics.helpers.DisplayElementHelper;

import javax.annotation.Nullable;

import static net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate;

public class RenderHolographicDisplay extends RenderEntity {

    public RenderHolographicDisplay(RenderManager manager) {
        super(manager);
    }

    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        //super.doRender(entity, x, y, z, entityYaw, partialTicks);
        if(!(entity instanceof EntityHolographicDisplay)){
            return;
        }
        EntityHolographicDisplay display_entity = (EntityHolographicDisplay) entity;
        TileAbstractHolographicDisplay display = display_entity.getHolographicDisplay();
        if(display == null || display.getGSI() == null || !display.isValid()){
            display_entity.setDead();
            return;
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0, 240);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-display_entity.rotationYaw, 0, 1, 0);
        GlStateManager.rotate(display_entity.rotationPitch, 1, 0, 0);
        GlStateManager.rotate(display_entity.rotationRoll, 0, 0, 1);

        tryBlendFuncSeparate(770, 1, 1, 0);
        GlStateManager.translate(0,0,0.005);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        float a = (float) (display.getScreenColour() >> 24 & 255) / 255.0F;
        float r = (float) (display.getScreenColour() >> 16 & 255) / 255.0F;
        float g = (float) (display.getScreenColour() >> 8 & 255) / 255.0F;
        float b = (float) (display.getScreenColour() & 255) / 255.0F;
        //float r = 100F / 255.0F, g=100F / 255.0F, b=200F / 255.0F, a= 100F / 255.0F;

        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        double left = -entity.width/2, top = -entity.height/2, right = entity.width/2, bottom = entity.height/2;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        bufferbuilder.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        GlStateManager.disableLighting();
        tessellator.draw();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();


        GlStateManager.translate(0,0,-0.005);

        GlStateManager.translate(entity.width/2,entity.height/2,0);
        GlStateManager.rotate(180, 0, 0, 1);
        display.getGSI().render();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    @Nullable
    protected ResourceLocation getEntityTexture(EntityHolographicDisplay entity)
    {
        return null;
    }
}
