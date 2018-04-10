package sonar.logistics.networking.events;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.core.api.utils.TileAdditionType;
import sonar.core.api.utils.TileRemovalType;
import sonar.logistics.api.cabling.ICable;

public class NetworkCableEvent extends Event {

	public final ICable tile;
	public final World world;
	
	public NetworkCableEvent(ICable tile, World world) {
		super();
		this.tile = tile;
		this.world = world;
	}

	public static class AddedCable extends NetworkCableEvent {
		
		public final TileAdditionType type;
		
		public AddedCable(ICable tile, World world, TileAdditionType type) {
			super(tile, world);
			this.type=type;
		}
	}

	public static class RemovedCable extends NetworkCableEvent {
		
		public final TileRemovalType type;
		
		public RemovedCable(ICable tile, World world, TileRemovalType type) {
			super(tile, world);
			this.type=type;
		}
	}
}
