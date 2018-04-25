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
import sonar.logistics.api.displays.DisplayGSI;
import sonar.logistics.api.tiles.displays.ConnectedDisplay;
import sonar.logistics.api.tiles.displays.DisplayScreenLook;
import sonar.logistics.api.tiles.displays.EnumDisplayFaceSlot;
import sonar.logistics.api.tiles.displays.IDisplay;
import sonar.logistics.common.multiparts.displays.TileLargeDisplayScreen;
import sonar.logistics.helpers.InteractionHelper;

public class GSIOverlays {

	public static DisplayScreenLook currentLook = null;
	public static BlockPos lastPos = null;

	@Nullable
	public static DisplayScreenLook getCurrentLook(DisplayGSI container) {
		if (currentLook != null && currentLook.identity == container.getDisplayGSIIdentity()) {
			return currentLook;
		}
		return null;
	}

	public static void tick(DrawBlockHighlightEvent evt) {
		EnumFacing face = evt.getTarget().sideHit;
		if (face != null) {
			BlockPos clickPos = evt.getTarget().getBlockPos();
			IPartSlot slot = EnumDisplayFaceSlot.fromFace(face);
			Optional<IMultipartTile> tile = MultipartHelper.getPartTile(evt.getPlayer().getEntityWorld(), clickPos, slot);
			if (tile.isPresent() && tile.get() instanceof IDisplay) {
				IDisplay display = (IDisplay) tile.get();
				if (display instanceof TileLargeDisplayScreen) {
					ConnectedDisplay connectedDisplay = ((TileLargeDisplayScreen) display).getConnectedDisplay();
					if (connectedDisplay != null) {
						display = connectedDisplay.getTopLeftScreen();
					}else{
						display = null;
					}
				}
				if (display != null && display.getGSI() != null) {
					Vec3d vec = evt.getTarget().hitVec;
					float hitX = (float) (vec.x - (double) clickPos.getX());
					float hitY = (float) (vec.y - (double) clickPos.getY());
					float hitZ = (float) (vec.z - (double) clickPos.getZ());
					currentLook = InteractionHelper.getLookPosition(display.getGSI(), clickPos, face, hitX, hitY, hitZ);
					return;
				}
			}

		}
		currentLook = null;

	}

}
