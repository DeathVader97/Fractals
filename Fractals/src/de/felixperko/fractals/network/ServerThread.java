package de.felixperko.fractals.network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ServerThread extends Thread {
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(3141);
			
			while (true) {
				System.out.println("server: waiting for connection...");
				Socket client = server.accept();
				System.out.println("server: connected.");
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
						System.out.println("server: latency: "+latency+" ms");
	//					break;
						out.println(System.nanoTime());
						out.flush();
					} catch (NoSuchElementException e) {
						System.out.println("server: lost connection to client.");
						connection_lost = true;
						break;
					}
				}
				System.out.println("server: closing connection...");
				out.println("close");
				out.flush();
				
				
				while (!connection_lost) {
					try {
						input = in.nextLine();
						System.out.println(input);
						if (input.equals("client: close ack")) {
							break;
						}
					} catch (NoSuchElementException e) {
						System.out.println("server: lost connection to client.");
						connection_lost = true;
					}
				}
				
				out.close();
				in.close();
				client.close();
				
				System.out.println("server: connection closed.");
				break;
			}
			
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
