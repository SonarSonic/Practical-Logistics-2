package sonar.logistics.client.gsi;

import java.util.Optional;

import javax.annotation.Nullable;

import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import sonar.logistics.api.displays.InfoContainer;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.helpers.InteractionHelper;

public class GSIOverlays {

	public static DisplayScreenLook currentLook = null;
	public static BlockPos lastPos = null;

	@Nullable
	public static DisplayScreenLook getCurrentLook(InfoContainer container) {
		if (currentLook != null && currentLook.identity == container.getDisplay().getInfoContainerID()) {
			return currentLook;
		}
		return null;
	}

	public static void tick(DrawBlockHighlightEvent evt) {
		EnumFacing face = evt.getTarget().sideHit;
		BlockPos clickPos = evt.getTarget().getBlockPos();
		if (face != null) {
			IPartSlot slot = EnumDisplayFaceSlot.fromFace(face);
			Optional<IMultipartTile> tile = MultipartHelper.getPartTile(evt.getPlayer().getEntityWorld(), clickPos, slot);
			if (tile.isPresent() && tile.get() instanceof IDisplay) {
				Vec3d vec = evt.getTarget().hitVec;
				float hitX = (float) (vec.x - (double) clickPos.getX());
				float hitY = (float) (vec.y - (double) clickPos.getY());
				float hitZ = (float) (vec.z - (double) clickPos.getZ());
				IDisplay display = (IDisplay) tile.get();
				if (display != null && display instanceof TileLargeDisplayScreen) {
					display = ((TileLargeDisplayScreen) display).getConnectedDisplay().getTopLeftScreen();
				}
				if (display != null) {
					currentLook = InteractionHelper.getLookPosition(display.container(), clickPos, face, hitX, hitY, hitZ);
					return;
				}
			}

		}
		currentLook = null;
	}

}
