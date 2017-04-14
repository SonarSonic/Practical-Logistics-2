package sonar.logistics.api.states;

import java.util.List;

import sonar.core.SonarCore;
import sonar.core.network.SonarClient;
import sonar.core.translate.ILocalisationHandler;
import sonar.core.translate.Localisation;

public enum TileMessage implements IState {

	// general tiles
	NO_NETWORK(false),

	// displays
	NO_READER_SELECTED(true),

	// data receivers
	NO_EMITTERS_CONNECTED(true), EMITTERS_OFFLINE(true),

	// readers
	NO_DATA_SELECTED(true),
	// inv reader
	NO_STACK_SELECTED(true), NO_FLUID_SELECTED(true),

	// signaller
	NO_STATEMENTS(true);

	public boolean canOpen;
	public Localisation message;

	TileMessage(boolean canOpen) {
		this.canOpen = canOpen;
		this.message = new Localisation("pl.states." + name().toLowerCase());
	}

	@Override
	public boolean canOpenTile() {
		return canOpen;
	}

	@Override
	public String getStateMessage() {
		return message.t();
	}

	@Override
	public int getStateID() {
		return ordinal();
	}

	public static List<Localisation> getLocalisations(List<Localisation> current) {
		for (TileMessage message : TileMessage.values()) {
			current.add(message.message);
		}
		return current;

	}

}
