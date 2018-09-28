package de.felixperko.fractals.server.network.messages;

import de.felixperko.fractals.server.network.Message;

public class ConnectedAckMessage extends Message{

	private static final long serialVersionUID = 4746386962938401924L;

	@Override
	protected void process() {
		log.log("Got answer from client!");
	}
	
}
