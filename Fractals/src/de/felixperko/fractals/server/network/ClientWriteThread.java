package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.util.CategoryLogger;

public class ClientWriteThread extends WriteThread{
	
	final static CategoryLogger superLogger = new CategoryLogger("com/client", Color.MAGENTA);
	
	public ClientWriteThread(Socket socket) throws UnknownHostException, IOException {
		super(socket);
		log = superLogger.createSubLogger("out");
		setListenLogger(new CategoryLogger("com/client/in", Color.MAGENTA));
		setConnection(FractalsMain.serverConnection);
	}
}
