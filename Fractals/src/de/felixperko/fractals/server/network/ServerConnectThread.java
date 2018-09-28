package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.FractalsServerMain;
import de.felixperko.fractals.server.util.CategoryLogger;

public class ServerConnectThread extends Thread{

	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	NetworkManager networkManager;
	
	public ServerConnectThread() {
		networkManager = FractalsServerMain.networkManager;
	}
	
	@Override
	public void run() {

		try {
			ServerSocket server = new ServerSocket(3141);
			log.log("Waiting for incoming connections...");
			while (!Thread.interrupted()) {
				//TODO manage write threads to send messages effortlessly
				ServerWriteThread serverWriteThread = FractalsMain.threadManager.startServerSocket(server.accept());
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				serverWriteThread.setClientInfo(networkManager.createNewClient(serverWriteThread));
			}
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
