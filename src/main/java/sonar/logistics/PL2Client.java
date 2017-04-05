package sonar.logistics;

import mcmultipart.client.multipart.MultipartRegistryClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.helpers.FontHelper;
import sonar.logistics.client.BlockRenderRegister;
import sonar.logistics.client.ClockRenderer;
import sonar.logistics.client.DisplayRenderer;
import sonar.logistics.client.ItemRenderRegister;
import sonar.logistics.client.RenderArray;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.RenderHammer;
import sonar.logistics.client.RenderOperatorOverlay;
import sonar.logistics.common.multiparts.ArrayPart;
import sonar.logistics.common.multiparts.ClockPart;
import sonar.logistics.common.multiparts.DisplayScreenPart;
import sonar.logistics.common.multiparts.HolographicDisplayPart;
import sonar.logistics.common.multiparts.LargeDisplayScreenPart;
import sonar.logistics.common.tileentity.TileEntityHammer;

public class PL2Client extends PL2Common {

	public void registerRenderThings() {
		ItemRenderRegister.register();
		BlockRenderRegister.register();

		MultipartRegistryClient.bindMultipartSpecialRenderer(DisplayScreenPart.class, new DisplayRenderer());
		MultipartRegistryClient.bindMultipartSpecialRenderer(HolographicDisplayPart.class, new DisplayRenderer());
		MultipartRegistryClient.bindMultipartSpecialRenderer(LargeDisplayScreenPart.class, new DisplayRenderer());
		MultipartRegistryClient.bindMultipartSpecialRenderer(ClockPart.class, new ClockRenderer());
		MultipartRegistryClient.bindMultipartSpecialRenderer(ArrayPart.class, new RenderArray());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHammer.class, new RenderHammer());
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void registerTextures() {
		// Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(InfoContainer.progressGreen);
	}

	public void load(FMLInitializationEvent event) {
		super.load(event);
		if (Minecraft.getMinecraft().getResourceManager() instanceof IReloadableResourceManager) {
			IReloadableResourceManager manager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
			manager.registerReloadListener(new PL2TranslationLoader());
		}
	}

	public static class PL2TranslationLoader implements IResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			PL2Translate.locals.forEach(l -> {

				l.toDisplay = FontHelper.translate(l.original);
				l.wasFound = !l.toDisplay.equals(l.original);
				if (!l.wasFound) {
					PL2.logger.info("NO TRANSLATION FOUND FOR: " + l.o());
				}
			});
			PL2.logger.info(PL2Constants.NAME + " Translations were updated");
		}
	}
	
	@SubscribeEvent
	public void renderWorldLastEvent(RenderWorldLastEvent evt) {
		RenderBlockSelection.tick(evt);
	}

	@SubscribeEvent
	public void renderHighlight(DrawBlockHighlightEvent evt) {
		RenderOperatorOverlay.tick(evt);
	}

	public void setUsingOperator(boolean bool) {
		RenderOperatorOverlay.isUsing = bool;
	}

	public boolean isUsingOperator() {
		return RenderOperatorOverlay.isUsing;
	}
}
