package sonar.logistics.base.events.types;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.logistics.api.core.tiles.connections.ICable;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;

public class NetworkCableEvent extends Event {

	public final ICable tile;
	public final World world;
	
	public NetworkCableEvent(ICable tile, World world) {
		super();
		this.tile = tile;
		this.world = world;
	}

	public static class AddedCable extends NetworkCableEvent {
		
		public final PL2AdditionType type;
		
		public AddedCable(ICable tile, World world, PL2AdditionType type) {
			super(tile, world);
			this.type=type;
		}
	}

	public static class RemovedCable extends NetworkCableEvent {
		
		public final PL2RemovalType type;
		
		public RemovedCable(ICable tile, World world, PL2RemovalType type) {
			super(tile, world);
			this.type=type;
		}
	}
}
