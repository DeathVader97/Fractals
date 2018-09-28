package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.felixperko.fractals.server.util.CategoryLogger;

public class ClientWriteThread extends WriteThread{
	
	final static CategoryLogger superLogger = new CategoryLogger("com/client", Color.MAGENTA);
	
	public ClientWriteThread() throws UnknownHostException, IOException {
		super(new Socket("localhost", 3141));
		log = superLogger.createSubLogger("out");
	}
}
