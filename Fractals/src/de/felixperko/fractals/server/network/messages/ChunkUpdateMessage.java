package de.felixperko.fractals.server.network.messages;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.network.Message;

public class ChunkUpdateMessage extends Message {
	
	private static final long serialVersionUID = -2349690041977280160L;
	
	Chunk chunk;
	
	public ChunkUpdateMessage(Chunk chunk) {
		this.chunk = chunk;
		log_received = false;
	}

	@Override
	protected void process() {
		FractalsMain.threadManager.getCalcPixelThread(FractalsMain.mainWindow.getMainRenderer()).addChunk(chunk);
	}

	public Chunk getChunk() {
		return chunk;
	}

	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}
}
