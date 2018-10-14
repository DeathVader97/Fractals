package de.felixperko.fractals.client.rendering.chunkprovider;

import de.felixperko.fractals.server.data.Chunk;
import de.felixperko.fractals.server.util.Position;

public interface ChunkProvider {

	public void addChunk(Chunk chunk);
	public Chunk getChunk(Position gridPos);
	public void reset();
}
