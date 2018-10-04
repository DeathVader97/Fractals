package de.felixperko.fractals.server.state;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.network.messages.StateChangedMessage;

public class ServerStateChangeListener<T> extends StateChangeListener<T> {
	
	public ServerStateChangeListener(State<T> state) {
		super(state);
	}
	
	@Override
	public void valueChanged(T oldValue, T newValue) {
		FractalsMain.messenger.writeMessageToServer(new StateChangedMessage<T>(FractalsMain.clientStateHolder.stateClientInfo.getValue(), null, actions, oldValue, newValue));
		super.valueChanged(oldValue, newValue);
	}

	@Override
	public void updateIfChanged(boolean resetChanged) {
		if (!changed)
			return;
		if (resetChanged)
			changed = false;
	}
}
