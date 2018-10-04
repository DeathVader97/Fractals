package de.felixperko.fractals.server.network.messages;

import java.util.List;

import de.felixperko.fractals.server.network.Message;
import de.felixperko.fractals.server.network.SenderInfo;
import de.felixperko.fractals.server.state.StateChangeAction;

public class StateChangedMessage<T> extends Message {
	
	private static final long serialVersionUID = 1492958056002988298L;
	
	List<StateChangeAction> actions;
	T oldValue;
	T newValue;
	
	public StateChangedMessage(SenderInfo sender, Message lastMessage, List<StateChangeAction> actions, T oldValue,
			T newValue) {
		super(sender, lastMessage);
		this.actions = actions;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	protected void process() {
		actions.forEach(a -> a.update());
	}

}
