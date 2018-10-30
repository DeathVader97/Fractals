package de.felixperko.fractals.server.network;

import java.awt.Color;

import de.felixperko.fractals.server.util.CategoryLogger;

public class ClientLocalConnection implements ClientConnection {
	
	SenderInfo senderInfo;
	
	CategoryLogger log = new CategoryLogger("com/local", Color.MAGENTA);
	
	public ClientLocalConnection(SenderInfo localSenderInfo) {
		this.senderInfo = localSenderInfo;
	}

	@Override
	public SenderInfo getSenderInfo() {
		return senderInfo;
	}
	
	@Override
	public void writeMessage(Message msg) {
		msg.received(this, log);
	}

}
