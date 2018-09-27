package de.felixperko.fractals.server.network;

public class ClientInfo {
	
	SenderInfo info;
	ServerWriteThread writeThread;
	
	public ClientInfo(SenderInfo info, ServerWriteThread writeThread) {
		this.info = info;
		this.writeThread = writeThread;
	}
	
	public void writeMessage(Message msg) {
		writeThread.writeMessage(msg);
	}

	public SenderInfo getSenderInfo() {
		return info;
	}
}
