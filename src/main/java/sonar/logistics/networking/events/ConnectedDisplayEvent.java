package sonar.logistics.networking.events;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.logistics.api.displays.tiles.ConnectedDisplay;

public class ConnectedDisplayEvent extends Event {

	public final ConnectedDisplay tile;
	public final World world;
	
	public ConnectedDisplayEvent(ConnectedDisplay tile, World world) {
		super();
		this.tile = tile;
		this.world = world;
	}

	public static class AddedConnectedDisplay extends ConnectedDisplayEvent {
				
		public AddedConnectedDisplay(ConnectedDisplay tile, World world) {
			super(tile, world);
		}
	}

	public static class RemovedConnectedDisplay extends ConnectedDisplayEvent {
				
		public RemovedConnectedDisplay(ConnectedDisplay tile, World world) {
			super(tile, world);
		}
	}
}
