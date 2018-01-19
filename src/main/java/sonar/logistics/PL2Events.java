package sonar.logistics;

import mcmultipart.MCMultiPart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import sonar.core.api.utils.BlockInteractionType;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.displays.BlockAbstractDisplay;
import sonar.logistics.common.multiparts.displays.TileAbstractDisplay;
import sonar.logistics.common.multiparts.nodes.TileArray;
import sonar.logistics.helpers.CableHelper;
import sonar.logistics.helpers.InteractionHelper;
import sonar.logistics.networking.connections.CableConnectionHandler;
import sonar.logistics.networking.connections.WirelessDataHandler;
import sonar.logistics.worlddata.ConnectedDisplayData;
import sonar.logistics.worlddata.IdentityCountData;

public class PL2Events {

	public static final int saveDimension = 0;
	public static int coolDownClick = 0;

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.side == Side.SERVER && event.phase == Phase.END) {
			CableConnectionHandler.instance().tick();
			PL2.getNetworkManager().tick();
			PL2.getServerManager().onServerTick();
			PL2.getWirelessManager().tick();
			PL2.getDisplayManager().tick();
			TileArray.entityChanged = false;

		} else {
			if (coolDownClick != 0) {
				coolDownClick--;
			}
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if (!event.getWorld().isRemote) {
			IdentityCountData.get(event.getWorld());
			ConnectedDisplayData.get(event.getWorld());

		}
	}

	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event) {
		if (!event.getWorld().isRemote) {
			IdentityCountData.get(event.getWorld());
			ConnectedDisplayData.get(event.getWorld());
		}
	}

	@SubscribeEvent
	public void onEntityJoin(EntityJoinWorldEvent event) {
		TileArray.entityChanged = true;
	}

	@SubscribeEvent
	public void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {

		BlockPos pos = event.getPos();
		World world = event.getWorld();
		if (world.isRemote && coolDownClick == 0) {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if (block == MCMultiPart.multipart) {
				RayTraceResult result = InteractionHelper.getRayTraceEyes(event.getEntityPlayer(), event.getWorld());
				IDisplay display = CableHelper.getDisplay(world, pos, EnumDisplayFaceSlot.fromFace(event.getFace()));
				if (display != null && display instanceof TileAbstractDisplay) {
					Vec3d vec = event.getHitVec();
					coolDownClick = 3;
					display.container().onClicked((TileAbstractDisplay) display, event.getEntityPlayer().isSneaking() ? BlockInteractionType.SHIFT_LEFT : BlockInteractionType.LEFT, world, pos, state, event.getEntityPlayer(), event.getHand(), event.getFace(), (float) vec.x - pos.getX(), (float) vec.y - pos.getY(), (float) vec.z - pos.getZ());
					event.setCanceled(true);
				}
			}
		}

	}
}