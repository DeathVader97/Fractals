package de.felixperko.fractals.server.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientWriteThread extends WriteThread{
	
	public ClientWriteThread() throws UnknownHostException, IOException {
		super(new Socket("localhost", 3141));
	}
}
