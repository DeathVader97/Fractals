package de.felixperko.fractals.server.network.messages;

import de.felixperko.fractals.server.network.Message;
import de.felixperko.fractals.server.util.CategoryLogger;

public class ReachableRequestMessage extends Message {

	private static final long serialVersionUID = 7296165361042593042L;

	@Override
	protected void process() {
		answer(new ReachableResponseMessage());
	}

}
