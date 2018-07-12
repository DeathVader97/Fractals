package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import de.felixperko.fractals.util.CategoryLogger;

public class ServerThread extends Thread {
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	Socket client;
	
	public ServerThread(Socket socket) {
		this.client = socket;
	}

	@Override
	public void run() {
		try {
			
			while (true) {
				log.log("waiting for connection...");
				log.log("connected.");
				Scanner in = new Scanner(client.getInputStream());
				PrintWriter out = new PrintWriter(client.getOutputStream());
				String input;
				boolean connection_lost = false;
				
				while (!connection_lost) {
					try {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						out.println("poll time");
						out.flush();
						input = in.nextLine();
						log.log("recieved: "+input);
						if (input.equals("client: closing connection")) {
							break;
						}
					} catch (NoSuchElementException e) {
						log.log("lost connection to client.");
						connection_lost = true;
					}
				}
				
				out.close();
				in.close();
				client.close();
				
				log.log("connection closed.");
				break;
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
