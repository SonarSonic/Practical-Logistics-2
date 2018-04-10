package sonar.logistics;

import mcmultipart.MCMultiPart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.networking.LogisticsNetworkHandler;
import sonar.logistics.networking.ServerInfoHandler;
import sonar.logistics.networking.cabling.CableConnectionHandler;
import sonar.logistics.networking.cabling.CableHelper;
import sonar.logistics.networking.cabling.RedstoneConnectionHandler;
import sonar.logistics.networking.cabling.WirelessDataManager;
import sonar.logistics.networking.cabling.WirelessRedstoneManager;
import sonar.logistics.networking.displays.ChunkViewerHandler;
import sonar.logistics.networking.displays.DisplayHandler;

public class PL2Events {

	public static final int saveDimension = 0;
	public static int coolDownClick = 0;
	public static long tickStart = 0;
	public static long updateTick = 0; //in nano seconds

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END) {
			tickStart = System.nanoTime();

			ChunkViewerHandler.instance().tick();
			CableConnectionHandler.instance().tick();
			RedstoneConnectionHandler.instance().tick();
			LogisticsNetworkHandler.instance().tick();
			ServerInfoHandler.instance().tick();	
					
			WirelessDataManager.instance().tick();
			WirelessRedstoneManager.instance().tick();
			DisplayHandler.instance().tick();
			TileArray.entityChanged = false;
			
			updateTick = System.nanoTime() - tickStart;
		} else {
			if (coolDownClick != 0) {
				coolDownClick--;
			}
		}
	}

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event) {
		TileArray.entityChanged = true;
	}

	@SubscribeEvent
	public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
		//multiparts dont implement left clicking properly.
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		if (coolDownClick == 0) {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block == MCMultiPart.multipart) {
				//RayTraceResult result = RayTraceHelper.getRayTraceEyes(event.getEntityPlayer(), event.getWorld());
				IDisplay display = CableHelper.getDisplay(world, pos, EnumDisplayFaceSlot.fromFace(event.getFace()));
				if (display != null && display instanceof TileAbstractDisplay) {
					Vec3d vec = event.getHitVec();
					coolDownClick = 3;
					display.getGSI().onClicked((TileAbstractDisplay) display, event.getEntityPlayer().isSneaking() ? BlockInteractionType.SHIFT_LEFT : BlockInteractionType.LEFT, world, pos, state, event.getEntityPlayer(), event.getHand(), event.getFace(), (float) vec.x - pos.getX(), (float) vec.y - pos.getY(), (float) vec.z - pos.getZ());
					event.setCanceled(true);
				}
			}
		}

	}
}