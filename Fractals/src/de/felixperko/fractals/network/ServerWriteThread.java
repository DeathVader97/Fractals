package de.felixperko.fractals.network;

import java.net.Socket;

public class ServerWriteThread extends WriteThread {
	
	public ServerWriteThread(Socket socket) {
		super(socket);
	}

	@Override
	public void run() {
		log.log("connected to client.");
		super.run();
	}
}
