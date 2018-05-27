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
	
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(3141);
			
			while (true) {
				log.log("waiting for connection...");
				Socket client = server.accept();
				log.log("connected.");
				Scanner in = new Scanner(client.getInputStream());
				PrintWriter out = new PrintWriter(client.getOutputStream());
				String input;
				boolean connection_lost = false;
				for(int i = 0 ; i < 10000 ; i++) {
					try {
						String input2 = in.nextLine();
						input = in.nextLine();
						long sendTime = Long.parseLong(input);
						double latency = (System.nanoTime()-sendTime)/1000000.;
						System.out.println(input2);
						log.log("latency: "+latency+" ms");
	//					break;
						out.println(System.nanoTime());
						out.flush();
					} catch (NoSuchElementException e) {
						log.log("server: lost connection to client.");
						connection_lost = true;
						break;
					}
				}
				log.log("closing connection...");
				out.println("close");
				out.flush();
				
				
				while (!connection_lost) {
					try {
						input = in.nextLine();
						log.log("recieved: "+input);
						if (input.equals("client: close ack")) {
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
			
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
