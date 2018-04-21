package sonar.logistics.api.states;

import java.util.ArrayList;
import java.util.List;

import sonar.core.translate.Localisation;

public enum ErrorMessage implements IState {

	// general tiles
	NO_NETWORK(false),

	// displays
	NO_READER_SELECTED(true),

	// data receivers
	NO_EMITTERS_CONNECTED(true), //
	EMITTERS_OFFLINE(true),

	// readers
	NO_DATA_SELECTED(true),
	
	// inv reader
	NO_STACK_SELECTED(true), //
	NO_FLUID_SELECTED(true),

	// signaller
	NO_STATEMENTS(true),
	
	READER_DISCONNECTED(true),
	
	READER_DESTROYED(true);

	public boolean canOpen;
	public Localisation message;

	ErrorMessage(boolean canOpen) {
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
		for (ErrorMessage message : ErrorMessage.values()) {
			current.add(message.message);
		}
		return current;
	}

	public static byte[] toBytes(List<ErrorMessage> errors) {
		byte[] bytes = new byte[errors.size()];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) errors.get(i).ordinal();
		}
		return bytes;
	}

	public static List<ErrorMessage> fromBytes(byte[] bytes) {
		List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
		for (int i = 0; i < bytes.length; i++) {
			errors.add(ErrorMessage.values()[bytes[i]]);
		}
		return errors;
	}
}
