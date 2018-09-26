package de.felixperko.fractals.network;

import java.io.ObjectInputStream;

import de.felixperko.fractals.Tasks.threading.FractalsThread;

public class ListenThread extends FractalsThread {
	
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
				msg.process();
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
