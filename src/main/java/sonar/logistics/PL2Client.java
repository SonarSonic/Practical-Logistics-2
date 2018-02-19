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
import sonar.logistics.api.states.TileMessage;
import sonar.logistics.client.ClockRenderer;
import sonar.logistics.client.DisplayRenderer;
import sonar.logistics.client.RenderArray;
import sonar.logistics.client.RenderBlockSelection;
import sonar.logistics.client.RenderHammer;
import sonar.logistics.client.RenderInteractionOverlay;
import sonar.logistics.client.RenderOperatorOverlay;
import sonar.logistics.client.gsi.GSIOverlays;
import sonar.logistics.client.gsi.GSIRegistry;
import sonar.logistics.client.gsi.IGSIRegistry;
import sonar.logistics.client.gsi.info.GSIAE2DriveInfo;
import sonar.logistics.client.gsi.info.GSIBasicInfo;
import sonar.logistics.client.gsi.info.GSIClockInfo;
import sonar.logistics.client.gsi.info.GSIEnergyStack;
import sonar.logistics.client.gsi.info.GSIFluidStack;
import sonar.logistics.client.gsi.info.GSIItemStack;
import sonar.logistics.client.gsi.info.GSILogicList;
import sonar.logistics.client.gsi.info.GSINoData;
import sonar.logistics.client.gsi.info.GSIProgressInfo;
import sonar.logistics.common.hammer.TileEntityHammer;
import sonar.logistics.common.multiparts.displays.TileDisplayScreen;
import sonar.logistics.common.multiparts.displays.TileHolographicDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.common.multiparts.misc.TileClock;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.guide.GuidePageRegistry;
import sonar.logistics.info.types.AE2DriveInfo;
import sonar.logistics.info.types.ClockInfo;
import sonar.logistics.info.types.InfoError;
import sonar.logistics.info.types.LogicInfo;
import sonar.logistics.info.types.LogicInfoList;
import sonar.logistics.info.types.MonitoredEnergyStack;
import sonar.logistics.info.types.MonitoredFluidStack;
import sonar.logistics.info.types.MonitoredItemStack;
import sonar.logistics.info.types.ProgressInfo;

public class PL2Client extends PL2Common implements ILocalisationHandler {
	
	IGSIRegistry GSI_REGISTRY = new GSIRegistry();
	
	public void registerRenderThings() {
		
		ClientRegistry.bindTileEntitySpecialRenderer(TileArray.class, new RenderArray());		
		ClientRegistry.bindTileEntitySpecialRenderer(TileDisplayScreen.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileHolographicDisplay.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileLargeDisplayScreen.class, new DisplayRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileClock.class, new ClockRenderer());		
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHammer.class, new RenderHammer());
		
		/* FIXME?
		GSI_REGISTRY.register(AE2DriveInfo.id, GSIAE2DriveInfo.class);
		GSI_REGISTRY.register(LogicInfo.id, GSIBasicInfo.class);		
		GSI_REGISTRY.register(ClockInfo.id, GSIClockInfo.class);
		GSI_REGISTRY.register(MonitoredEnergyStack.id, GSIEnergyStack.class);
		GSI_REGISTRY.register(MonitoredFluidStack.id, GSIFluidStack.class);
		GSI_REGISTRY.register(MonitoredItemStack.id, GSIItemStack.class);
		GSI_REGISTRY.register(LogicInfoList.id, GSILogicList.class);
		GSI_REGISTRY.register(InfoError.id, GSINoData.class);
		GSI_REGISTRY.register(ProgressInfo.id, GSIProgressInfo.class);
		*/
	}

	public IGSIRegistry getGSIRegistry(){
		return GSI_REGISTRY;
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
	public void renderInteractionOverlay(RenderGameOverlayEvent.Post evt){
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
