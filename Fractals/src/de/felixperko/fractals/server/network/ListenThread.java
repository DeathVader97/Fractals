package de.felixperko.fractals.server.network;

import java.awt.Color;
import java.io.ObjectInputStream;

import de.felixperko.fractals.server.threads.FractalsThread;
import de.felixperko.fractals.server.util.CategoryLogger;

public class ListenThread extends FractalsThread {

	CategoryLogger log = new CategoryLogger("com/server", Color.MAGENTA);
	
	static int ID_COUNTER = 0;
	
	WriteThread readThread;
	ObjectInputStream in;
	boolean closeConnection = false;

	public ListenThread(WriteThread readThread, ObjectInputStream in) {
		super("listenThread_"+ID_COUNTER++, 5);
		this.readThread = readThread;
		this.in = in;
	}
	
	@Override
	public void run() {

		while (!closeConnection) {
			try {
				setPhase(PHASE_WAITING);
				Message msg = (Message) in.readObject();
				setPhase(PHASE_WORKING);
				msg.received(log);
				if (isCloseConnection())
					readThread.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isCloseConnection() {
		return closeConnection;
	}

	public void setCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
	}
}
