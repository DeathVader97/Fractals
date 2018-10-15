package de.felixperko.fractals.server.network;

public class ClientConnection implements Connection{
	
	SenderInfo info;
	ServerWriteThread writeThread;
	
	public ClientConnection(SenderInfo info, ServerWriteThread writeThread) {
		this.info = info;
		this.writeThread = writeThread;
	}
	
	@Override
	public void writeMessage(Message msg) {
		writeThread.writeMessage(msg);
	}

	public SenderInfo getSenderInfo() {
		return info;
	}
}
