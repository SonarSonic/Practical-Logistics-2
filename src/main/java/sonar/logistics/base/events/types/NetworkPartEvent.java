package sonar.logistics.base.events.types;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.logistics.base.tiles.INetworkTile;
import sonar.logistics.base.utils.PL2AdditionType;
import sonar.logistics.base.utils.PL2RemovalType;

public class NetworkPartEvent<T extends INetworkTile> extends Event {

	public final T tile;
	public final World world;
	
	public NetworkPartEvent(T tile, World world) {
		super();
		this.tile = tile;
		this.world = world;
	}

	public static class AddedPart extends NetworkPartEvent {
		
		public final PL2AdditionType type;
		
		public AddedPart(INetworkTile tile, World world, PL2AdditionType type) {
			super(tile, world);
			this.type=type;
		}
	}

	public static class RemovedPart extends NetworkPartEvent {
		
		public final PL2RemovalType type;
		
		public RemovedPart(INetworkTile tile, World world, PL2RemovalType type) {
			super(tile, world);
			this.type=type;
		}
	}
	
}
