package sonar.logistics;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sonar.core.network.SonarClient;
import sonar.core.translate.ILocalisationHandler;
import sonar.core.translate.Localisation;
import sonar.logistics.api.IInfoManager;
import sonar.logistics.api.displays.elements.text.StyledStringHelper;
import sonar.logistics.api.displays.elements.text.StyledStringRenderer;
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.client.ClockRenderer;
import sonar.logistics.client.DisplayRenderer;
import sonar.logistics.client.RenderArray;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.RenderHammer;
import sonar.logistics.client.RenderInteractionOverlay;
import sonar.logistics.client.RenderOperatorOverlay;
import sonar.logistics.client.gsi.GSIOverlays;
import sonar.logistics.common.hammer.TileEntityHammer;
import sonar.logistics.common.multiparts.displays.TileDisplayScreen;
import sonar.logistics.common.multiparts.displays.TileHolographicDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.common.multiparts.misc.TileClock;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.guide.GuidePageRegistry;
import sonar.logistics.networking.ClientInfoHandler;

public class PL2Client extends PL2Common implements ILocalisationHandler {

	public ClientInfoHandler client_info_manager;

	public void registerRenderThings() {

		ClientRegistry.bindTileEntitySpecialRenderer(TileArray.class, new RenderArray());
		ClientRegistry.bindTileEntitySpecialRenderer(TileDisplayScreen.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileHolographicDisplay.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileLargeDisplayScreen.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileClock.class, new ClockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHammer.class, new RenderHammer());
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
		PL2.logger.info("Registered Client Event Handler");
		registerRenderThings();
		PL2.logger.info("Registered Renderers");
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

	public void setUsingOperator(boolean bool) {
		RenderOperatorOverlay.isUsing = bool;
	}

	public boolean isUsingOperator() {
		return RenderOperatorOverlay.isUsing;
	}

	@Override
	public List<Localisation> getLocalisations(List<Localisation> current) {
		current.addAll(PL2Translate.locals);
		current = TileMessage.getLocalisations(current);
		return current;
	}
}
