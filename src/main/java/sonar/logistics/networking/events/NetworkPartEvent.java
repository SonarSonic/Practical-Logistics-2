package sonar.logistics.networking.events;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import sonar.core.api.utils.TileAdditionType;
import sonar.core.api.utils.TileRemovalType;
import sonar.logistics.api.cabling.INetworkTile;
import sonar.logistics.api.networks.ILogisticsNetwork;
import sonar.logistics.api.utils.PL2AdditionType;
import sonar.logistics.api.utils.PL2RemovalType;
import sonar.logistics.api.wireless.IDataReceiver;

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
