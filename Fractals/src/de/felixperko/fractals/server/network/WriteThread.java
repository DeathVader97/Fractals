package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import de.felixperko.fractals.server.threads.FractalsThread;
import de.felixperko.fractals.server.util.CategoryLogger;

public class WriteThread extends FractalsThread {
	
	static int ID_COUNTER = 0;
	
	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	ListenThread listenThread;
	boolean closeConnection = false;
	
	Queue<Message> pendingMessages = new LinkedList<>();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	protected Socket socket;
	
	public WriteThread(Socket socket) {
		super("writeThread_"+ID_COUNTER++, 5);
		this.socket = socket;
	}

	public void run() {
		try {
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			listenThread = new ListenThread(this, in);
			listenThread.start();
			
			while (!Thread.interrupted()) {
				
				while (pendingMessages.isEmpty()) {
					setPhase(PHASE_IDLE);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				setPhase(PHASE_WORKING);
				Iterator<Message> it = pendingMessages.iterator();
				while (it.hasNext()) {
					Message msg = it.next();
					msg.setSentTime();
					out.writeObject(msg);
					it.remove();
				}
				out.flush();
			}
			
			in.close();
			out.close();
			socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeMessage(Message msg) {
		pendingMessages.add(msg);
	}
	
	public void closeConnection() {
		interrupt();
	};
}
