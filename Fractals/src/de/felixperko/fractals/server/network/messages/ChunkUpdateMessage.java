package de.felixperko.fractals.server.network.messages;

import de.felixperko.fractals.client.FractalsMain;
import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.network.Message;

public class ChunkUpdateMessage extends Message {
	
	Chunk chunk;
	
	public ChunkUpdateMessage(Chunk chunk) {
		this.chunk = chunk;
	}

	@Override
	protected void process() {
		FractalsMain.threadManager.getCalcPixelThread().addChunk(chunk);
	}

	public Chunk getChunk() {
		return chunk;
	}

	public void setChunk(Chunk chunk) {
		this.chunk = chunk;
	}
}
