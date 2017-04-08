package sonar.logistics.api.asm;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import sonar.logistics.api.info.InfoUUID;
import sonar.logistics.connections.monitoring.MonitoredList;

public class MonitoredListEvent extends Event {

	public final MonitoredList list;
	public final InfoUUID id;
	public final Side side;

	public MonitoredListEvent(MonitoredList list, InfoUUID id, Side side) {
		this.list = list;
		this.id = id;
		this.side = side;
	}

	public boolean canReadEvent(InfoUUID id, Side side) {
		return side.equals(this.side) && id.equals(this.id);
	}

	public static class CHANGED extends MonitoredListEvent {
		public CHANGED(MonitoredList list, InfoUUID id, Side side) {
			super(list, id, side);
		}
	}

}
