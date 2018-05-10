package sonar.logistics.api.core.tiles.displays.tiles;

import net.minecraft.util.math.Vec3d;
import sonar.logistics.api.core.tiles.connections.ICable;
import sonar.logistics.core.tiles.displays.tiles.connected.ConnectedDisplay;

import java.util.Optional;

public interface ILargeDisplay extends IDisplay, ICable {

	Optional<ConnectedDisplay> getConnectedDisplay();

	void setConnectedDisplay(ConnectedDisplay connectedDisplay);

	/** if this {@link ILargeDisplay} should render the info from the {@link ConnectedDisplay} */
    boolean shouldRender();

	/** sets if this {@link ILargeDisplay} should be responsible for rendering the data from the {@link ConnectedDisplay} */
    void setShouldRender(boolean shouldRender);

	@Override
	default Vec3d getScreenScaling(){
		Optional<ConnectedDisplay> display = getConnectedDisplay();
		return display.isPresent() ? display.get().getScreenScaling() : new Vec3d(0,0,0);
	}

	@Override
	default Vec3d getScreenRotation(){
		Optional<ConnectedDisplay> display = getConnectedDisplay();
		return display.isPresent() ? display.get().getScreenRotation() : new Vec3d(0,0,0);
	}

	@Override
	default Vec3d getScreenOrigin(){
		Optional<ConnectedDisplay> display = getConnectedDisplay();
		return display.isPresent() ? display.get().getScreenOrigin() : new Vec3d(0,0,0);
	}

}
