package sonar.logistics.networking.events;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.core.api.utils.TileAdditionType;
import sonar.core.api.utils.TileRemovalType;
import sonar.logistics.api.cabling.INetworkTile;

public class NetworkPartEvent extends Event {

	public final INetworkTile tile;
	public final World world;
	
	public NetworkPartEvent(INetworkTile tile, World world) {
		super();
		this.tile = tile;
		this.world = world;
	}

	public static class AddedPart extends NetworkPartEvent {
		
		public final TileAdditionType type;
		
		public AddedPart(INetworkTile tile, World world, TileAdditionType type) {
			super(tile, world);
			this.type=type;
		}
	}

	public static class RemovedPart extends NetworkPartEvent {
		
		public final TileRemovalType type;
		
		public RemovedPart(INetworkTile tile, World world, TileRemovalType type) {
			super(tile, world);
			this.type=type;
		}
	}
}
