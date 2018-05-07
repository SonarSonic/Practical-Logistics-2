package sonar.logistics.networking.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EventScheduler {

	//// EVENT HANDLING \\\\

	public Map<Event, Integer> scheduled_events = new HashMap<>();
	public Map<Runnable, Integer> scheduled_runnables = new HashMap<>();

	public void scheduleEvent(Event event, int ticksToWait) {
		scheduled_events.put(event, ticksToWait);
	}

	public void scheduleRunnable(Runnable action, int ticksToWait) {
		scheduled_runnables.put(action, ticksToWait);
	}

	public void flushEvents() {
		if (!scheduled_events.isEmpty()) {
			Iterator<Entry<Event, Integer>> it = scheduled_events.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Event, Integer> entry = it.next();
				if (entry.getValue() <= 0) {
					MinecraftForge.EVENT_BUS.post(entry.getKey());
					it.remove();
				} else {
					entry.setValue(entry.getValue() - 1);
				}
			}
		}
		if (!scheduled_runnables.isEmpty()) {
			Iterator<Entry<Runnable, Integer>> it = scheduled_runnables.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Runnable, Integer> entry = it.next();
				if (entry.getValue() <= 0) {
					entry.getKey().run();
					it.remove();
				} else {
					entry.setValue(entry.getValue() - 1);
				}
			}
		}
	}
}
