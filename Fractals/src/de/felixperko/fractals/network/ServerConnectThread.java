package de.felixperko.fractals.network;

import java.io.IOException;
import java.net.ServerSocket;

import de.felixperko.fractals.FractalsMain;

public class ServerConnectThread extends Thread{
	
	@Override
	public void run() {

		try {
			ServerSocket server = new ServerSocket(3141);
			while (!Thread.interrupted()) {
				FractalsMain.threadManager.startServerSocket(server.accept());
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
