package de.felixperko.fractals.network;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import de.felixperko.fractals.util.CategoryLogger;

public class ClientThread extends Thread{
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
//	@Override
//	public void run() {
//		try {
//			Socket socket = new Socket("localhost", 3141);
//			Scanner in = new Scanner(socket.getInputStream());
//			PrintWriter out = new PrintWriter(socket.getOutputStream());
//			String input;
//			out.println("client: established.");
//			out.println(System.nanoTime());
//			out.flush();
//			while (!(input = in.nextLine()).equals("close")) {
//				long sendTime = Long.parseLong(input);
//				double latency = (System.nanoTime()-sendTime)/1000000.;
////				System.out.println("client: latency: "+latency+" ms");
//				out.println("client: latency: "+latency+" ms");
//				out.println(System.nanoTime());
//				out.flush();
////				break;
//			}
//			out.println("client: close ack");
//			out.flush();
//			in.close();
//			out.close();
//			socket.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	public void run() {
		try {
			Socket socket = new Socket("localhost", 3141);
			Scanner in = new Scanner(socket.getInputStream());
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			out.println("client: connection established");
			out.flush();
			String input;
			while (!(input = in.nextLine()).equals("close")) {
				out.println(System.nanoTime());
				out.flush();
				log.log("sent time to server");
			}
			out.println("client: closing connection");
			out.flush();
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
}
