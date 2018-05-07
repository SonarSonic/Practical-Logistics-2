package sonar.logistics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.api.utils.BlockInteractionType;
import sonar.core.network.SonarClient;
import sonar.core.translate.ILocalisationHandler;
import sonar.core.translate.Localisation;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.displays.elements.text.StyledStringRenderer;
import sonar.logistics.api.errors.ErrorMessage;
import sonar.logistics.client.*;
import sonar.logistics.client.gsi.GSIOverlays;
import sonar.logistics.common.hammer.TileEntityHammer;
import sonar.logistics.common.multiparts.displays.TileDisplayScreen;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.common.multiparts.displays.TileMiniDisplay;
import sonar.logistics.common.multiparts.holographic.EntityHolographicDisplay;
import sonar.logistics.common.multiparts.misc.TileClock;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.guide.GuidePageRegistry;
import sonar.logistics.networking.ClientInfoHandler;

import java.util.List;

public class PL2Client extends PL2Common implements ILocalisationHandler {

	public ClientInfoHandler client_info_manager;

	public void registerRenderThings() {

		ClientRegistry.bindTileEntitySpecialRenderer(TileArray.class, new RenderArray());
		ClientRegistry.bindTileEntitySpecialRenderer(TileDisplayScreen.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileMiniDisplay.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileLargeDisplayScreen.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileClock.class, new ClockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHammer.class, new RenderHammer());
		RenderingRegistry.registerEntityRenderingHandler(EntityHolographicDisplay.class, (manager) -> new RenderHolographicDisplay(manager));
	}

	public ClientInfoHandler getClientManager() {
		return client_info_manager;
	}

	public IInfoManager getInfoManager(boolean isRemote) {
		return isRemote ? getClientManager() : server_info_manager;
	}

	public void initHandlers() {
		super.initHandlers();
		client_info_manager = new ClientInfoHandler();
		MinecraftForge.EVENT_BUS.register(client_info_manager);
		PL2.logger.info("Initialised Client Info Handler");
	}

	public void removeAll() {
		super.removeAll();
		client_info_manager.removeAll();
		PL2.logger.info("Cleared Client Info Handler");	
	}

	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		MinecraftForge.EVENT_BUS.register(this);
		PL2.logger.info("Initialised Client Event Handler");
		registerRenderThings();
		PL2.logger.info("Initialised Renderers");
	}

	public void load(FMLInitializationEvent event) {
		super.load(event);
		SonarClient.translator.add(this);
		if (Minecraft.getMinecraft().getResourceManager() instanceof IReloadableResourceManager) {
			IReloadableResourceManager manager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
			manager.registerReloadListener(StyledStringRenderer.instance());
		}
	}

	public void postLoad(FMLPostInitializationEvent evt) {
		super.postLoad(evt);
		GuidePageRegistry.init();
	}

	@SubscribeEvent
	public void renderWorldLastEvent(RenderWorldLastEvent evt) {
		RenderBlockSelection.tick(evt);
	}

	@SubscribeEvent
	public void renderHighlight(DrawBlockHighlightEvent evt) {
		if (Minecraft.getMinecraft().inGameHasFocus) {
			RenderOperatorOverlay.tick(evt);
			GSIOverlays.tick(evt);
		}
	}

	@SubscribeEvent
	public void renderInteractionOverlay(RenderGameOverlayEvent.Post evt) {
		RenderInteractionOverlay.tick(evt);
	}

	@SubscribeEvent
	public void onEntityAttack(AttackEntityEvent evt){
		if(evt.getTarget() instanceof EntityHolographicDisplay){
			EntityHolographicDisplay display = (EntityHolographicDisplay)evt.getTarget();
			display.doGSIInteraction(evt.getEntityPlayer(), evt.getEntityPlayer().isSneaking()?BlockInteractionType.SHIFT_LEFT : BlockInteractionType.LEFT, evt.getEntityPlayer().getActiveHand());
			evt.setCanceled(true);
		}
	}

	public void setUsingOperator(boolean bool) {
		RenderOperatorOverlay.isUsing = bool;
	}

	public boolean isUsingOperator() {
		return RenderOperatorOverlay.isUsing;
	}

	@Override
	public List<Localisation> getLocalisations(List<Localisation> current) {
		current.addAll(PL2Translate.locals);
		current = ErrorMessage.getLocalisations(current);
		return current;
	}
}
