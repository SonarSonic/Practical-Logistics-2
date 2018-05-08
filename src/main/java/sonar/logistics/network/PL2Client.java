package sonar.logistics.network;

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
import sonar.logistics.PL2;
import sonar.logistics.PL2Translate;
import sonar.logistics.api.base.IInfoManager;
import sonar.logistics.base.ClientInfoHandler;
import sonar.logistics.base.gui.overlays.OverlayBlockSelection;
import sonar.logistics.base.gui.overlays.OverlayItemInteraction;
import sonar.logistics.base.gui.overlays.OverlayOperatorInfo;
import sonar.logistics.base.guidance.errors.ErrorMessage;
import sonar.logistics.core.items.guide.GuidePageRegistry;
import sonar.logistics.core.tiles.displays.gsi.render.GSIOverlays;
import sonar.logistics.core.tiles.displays.info.types.text.utils.StyledStringRenderer;
import sonar.logistics.core.tiles.displays.tiles.connected.TileLargeDisplayScreen;
import sonar.logistics.core.tiles.displays.tiles.holographic.EntityHolographicDisplay;
import sonar.logistics.core.tiles.displays.tiles.render.DisplayRenderer;
import sonar.logistics.core.tiles.displays.tiles.render.RenderHolographicDisplay;
import sonar.logistics.core.tiles.displays.tiles.small.TileDisplayScreen;
import sonar.logistics.core.tiles.displays.tiles.small.TileMiniDisplay;
import sonar.logistics.core.tiles.misc.clock.TileClock;
import sonar.logistics.core.tiles.misc.clock.render.ClockRenderer;
import sonar.logistics.core.tiles.misc.hammer.TileEntityHammer;
import sonar.logistics.core.tiles.misc.hammer.render.RenderHammer;
import sonar.logistics.core.tiles.nodes.array.TileArray;
import sonar.logistics.core.tiles.nodes.array.render.RenderArray;

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
		OverlayBlockSelection.tick(evt);
	}

	@SubscribeEvent
	public void renderHighlight(DrawBlockHighlightEvent evt) {
		if (Minecraft.getMinecraft().inGameHasFocus) {
			OverlayOperatorInfo.tick(evt);
			GSIOverlays.tick(evt);
		}
	}

	@SubscribeEvent
	public void renderInteractionOverlay(RenderGameOverlayEvent.Post evt) {
		OverlayItemInteraction.tick(evt);
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
		OverlayOperatorInfo.isUsing = bool;
	}

	public boolean isUsingOperator() {
		return OverlayOperatorInfo.isUsing;
	}

	@Override
	public List<Localisation> getLocalisations(List<Localisation> current) {
		current.addAll(PL2Translate.locals);
		current = ErrorMessage.getLocalisations(current);
		return current;
	}
}
