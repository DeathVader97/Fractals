package de.felixperko.fractals.server.network;

import java.net.Socket;

import de.felixperko.fractals.server.FractalsServerMain;
import de.felixperko.fractals.server.network.messages.ConnectedMessage;

public class ServerWriteThread extends WriteThread {
	
	public ServerWriteThread(Socket socket) {
		super(socket);
	}
	
	ClientInfo clientInfo;

	@Override
	public void run() {
		log.log("connected to client.");
		super.run();
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
		writeMessage(new ConnectedMessage(clientInfo));
	}
}
