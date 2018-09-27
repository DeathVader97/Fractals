package de.felixperko.fractals.server.network;

public class Messenger {
	ClientWriteThread writeToServer;
	
	public void writeMessageToServer(Message message) {
		if (writeToServer == null)
			throw new IllegalStateException("Attempted to write a message but the 'write to server' thread wasn't set");
		writeToServer.writeMessage(message);
	}
	
	public ClientWriteThread getWriteToServer() {
		return writeToServer;
	}

	public void setWriteToServer(ClientWriteThread writeToServer) {
		this.writeToServer = writeToServer;
	}
}
