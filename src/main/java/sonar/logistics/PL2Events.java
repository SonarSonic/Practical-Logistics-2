package sonar.logistics;

import mcmultipart.MCMultiPart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
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
import sonar.logistics.networking.events.LogisticsEventHandler;

public class PL2Events {

	public static final int saveDimension = 0;
	public static int coolDownClick = 0;
	public static long tickStart = 0;
	public static long updateTick = 0; // in nano seconds

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END) {
			tickStart = System.nanoTime();

			LogisticsEventHandler.instance().triggerConstructingPhase();//

			LogisticsEventHandler.instance().triggerUpdatingPhase();//

			LogisticsEventHandler.instance().triggerNotifyingPhase();//

			TileArray.entityChanged = false;
			updateTick = System.nanoTime() - tickStart;
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (coolDownClick != 0) {
			coolDownClick--;
		}
	}

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event) {
		TileArray.entityChanged = true;
	}

	@SubscribeEvent
	public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
		// multiparts dont implement left clicking properly.
		BlockPos pos = event.getPos();
		World world = event.getWorld();
		EntityPlayer player = event.getEntityPlayer();
		if (pos != null && world != null && player != null) {
			if (coolDownClick == 0) {
				IBlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				if (block == MCMultiPart.multipart) {
					IDisplay display = CableHelper.getDisplay(world, pos, EnumDisplayFaceSlot.fromFace(event.getFace()));
					if (display != null && display instanceof TileAbstractDisplay) {
						if (display.getGSI() != null) {
							Vec3d vec = event.getHitVec();
							float vecX = (float) vec.x - pos.getX();
							float vecY = (float) vec.y - pos.getY();
							float vecZ = (float) vec.z - pos.getZ();
							coolDownClick = 3;
							BlockInteractionType interactionType = player.isSneaking() ? BlockInteractionType.SHIFT_LEFT : BlockInteractionType.LEFT;
							display.getGSI().onClicked((TileAbstractDisplay) display, interactionType, world, pos, state, player, event.getHand(), event.getFace(), vecX, vecY, vecZ);
							event.setCanceled(true);
						}
					}
				}
			}
		}

	}
}